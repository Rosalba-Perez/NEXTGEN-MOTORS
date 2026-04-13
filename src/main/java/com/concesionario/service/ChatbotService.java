package com.concesionario.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.concesionario.repository.VehiculoRepository;
import com.concesionario.model.Vehiculo;
import java.util.List;
import java.util.Comparator;
import java.util.Arrays;
import java.text.Normalizer;

/**
 * Servicio de Chatbot impulsado por Groq (vía OpenAI compatibility layer)
 */
@Service
public class ChatbotService {

    private final ChatModel chatModel;
    private final VehiculoRepository vehiculoRepository;

    public ChatbotService(ChatModel chatModel, VehiculoRepository vehiculoRepository) {
        this.chatModel = chatModel;
        this.vehiculoRepository = vehiculoRepository;
    }
    
    // Récord auxiliar para puntuar los vehículos
    private record VehiculoScore(Vehiculo vehiculo, int score) {}

    // Elimina tildes/acentos para comparación flexible
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                         .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    public String analizarYResponder(String mensajeUsuario, List<Map<String, String>> historial) {
        String mensajeMin = mensajeUsuario.toLowerCase();
        
        // 1. Extraer palabras clave y expandirlas contextualmente
        List<String> palabrasOriginales = Arrays.stream(mensajeMin.replaceAll("[^a-záéíóúñ0-9\\s]", "").split("\\s+"))
                                           .filter(p -> p.length() > 2)
                                           .toList();
        
        // Expansión de términos para búsqueda contextual
        StringBuilder regexBuilder = new StringBuilder();
        for (String p : palabrasOriginales) {
            if (regexBuilder.length() > 0) regexBuilder.append("|");
            regexBuilder.append(p);
            
            // Sinónimos y conceptos relacionados mejorados (fuzzy context)
            if (p.startsWith("rapid") || p.startsWith("veloz") || p.contains("velocidad")) {
                regexBuilder.append("|potencia|aceleración|rendimiento|0-100|pista|deportivo|performance|rápido|rapido");
            } else if (p.contains("viaja") || p.contains("carretera") || p.contains("pasear")) {
                regexBuilder.append("|comodidad|confort|premium|autovía|distancia|largo|crucero|viajar|viaje");
            } else if (p.contains("familia") || p.contains("hijo") || p.contains("niño")) {
                regexBuilder.append("|espacio|seguridad|pasajeros|asientos|amplio|familiar");
            } else if (p.contains("campo") || p.contains("finca") || p.contains("offroad") || p.contains("todoterreno") || p.contains("montaña")) {
                regexBuilder.append("|4x4|tracción|terreno|robusto|aventura|suspensión|todoterreno|barro");
            } else if (p.contains("automati") || p.contains("automatic")) {
                regexBuilder.append("|automática|automatica|transmisión|caja");
            }
        }
                                           
        List<Vehiculo> vehiculosFiltrados;
        if (regexBuilder.length() > 0) {
            vehiculosFiltrados = vehiculoRepository.findByFiltroRegex(regexBuilder.toString());
        } else {
            vehiculosFiltrados = vehiculoRepository.findByDestacadoTrue();
        }

        if (vehiculosFiltrados == null || vehiculosFiltrados.isEmpty()) {
            vehiculosFiltrados = vehiculoRepository.findByDestacadoTrue();
        }
                                           
        // 2. Calcular puntaje con pesos contextuales
        List<Vehiculo> mejoresOpciones = vehiculosFiltrados.stream()
            .map(v -> {
                int score = 0;
                String desc = normalizarTexto(v.getDescripcion());
                String marc = normalizarTexto(v.getMarca());
                String mod  = normalizarTexto(v.getModelo());
                // Normalizar categoría para ignorar tildes ("Híbridos" → "hibridos")
                String cat  = normalizarTexto(v.getCategoria());
                // Normalizar también el término de búsqueda al comparar
                String mensajeNorm = normalizarTexto(mensajeUsuario);
                
                String[] terminosBusqueda = regexBuilder.toString().split("\\|");
                for (String t : terminosBusqueda) {
                    String tNorm = normalizarTexto(t);
                    if (marc.contains(tNorm) || mod.contains(tNorm)) {
                        score += 5;
                    } else if (cat.contains(tNorm)) {
                        score += 3;
                    } else if (desc.contains(tNorm)) {
                        score += 4;
                    }
                }
                // Bonus si la categoría aparece literalmente en el mensaje (sin tilde)
                if (!cat.isEmpty() && mensajeNorm.contains(cat)) score += 4;
                
                if (score == 0 && v.isDestacado()) score = 1;
                return new VehiculoScore(v, score);
            })
            .sorted(Comparator.comparingInt(VehiculoScore::score).reversed())
            .limit(4)
            .map(VehiculoScore::vehiculo)
            .toList();

        // 3. Crear el inventario reducido
        StringBuilder inventario = new StringBuilder("\nOPCIONES RECOMENDADAS PRE-FILTRADAS:\n");
        for (Vehiculo v : mejoresOpciones) {
            inventario.append("- ID: [").append(v.getId()).append("] | ")
                      .append(v.getMarca()).append(" ").append(v.getModelo())
                      .append(" (").append(v.getCategoria()).append(") | Precio: $").append(v.getPrecio()).append("\n")
                      .append("  Descripción: ").append(v.getDescripcion() != null ? v.getDescripcion() : "Sin descripción detallada.")
                      .append("\n\n");
        }

        // 4. Preparar lista de mensajes con HISTORIAL
        List<Message> mensajes = new ArrayList<>();
        mensajes.add(new SystemMessage("""
            Eres Dante, el asesor experto y asistente virtual de la concesionaria 'NextGen Motors'. 
            Tu meta es ser útil, persuasivo y amable.
            
            REGLA DE ORO DE MEMORIA: Tienes acceso a la conversación previa. No te presentes de nuevo si ya lo hiciste.
             Si el usuario hace una pregunta de seguimiento ("¿cuál es ese?", "¿cuánto cuesta ese?"), responde refiriéndote al último vehículo mencionado.
            
            REGLA DE ORO DE RESPUESTA:
            1. SIEMPRE debes mencionar explícitamente la MARCA y el MODELO del vehículo que recomiendas.
            2. SIEMPRE debes incluir el ID con el formato [[ID: id_del_vehiculo]] pegado al nombre.
            3. Tus respuestas DEBEN SER BREVES (máximo 3 oraciones).
            4. RESTRICCIÓN DE ALCANCE ABSOLUTA E INQUEBRANTABLE: Tu único propósito es asistir en temas de 'NextGen Motors', vehículos, movilidad y compra de autos. 
            Si el usuario pregunta CUALQUIER COSA fuera de este dominio (política, historia, geografía, personas públicas, cultura general, etc.), debes responder ÚNICA Y EXCLUSIVAMENTE con esta frase, 
            sin agregar nada más: "No está en mis funcionalidades responder a eso". NUNCA menciones vehículos en ese caso.
            
            
            Ejemplo correcto de recomendación: "Te recomiendo el Mercedes AMG GT [[ID: 64bf21...]] porque tiene un rendimiento excepcional en carretera."
            
            Vehículos disponibles actualmente para este contexto:
            """ + inventario.toString() + """
            """));

        // Añadir historial al contexto
        if (historial != null) {
            for (Map<String, String> msg : historial) {
                if ("user".equals(msg.get("role"))) {
                    mensajes.add(new UserMessage(msg.get("content")));
                } else {
                    mensajes.add(new AssistantMessage(msg.get("content")));
                }
            }
        }

        // Añadir mensaje actual
        mensajes.add(new UserMessage(mensajeUsuario));

        return chatModel.call(new Prompt(mensajes)).getResult().getOutput().getText();
    }
}
