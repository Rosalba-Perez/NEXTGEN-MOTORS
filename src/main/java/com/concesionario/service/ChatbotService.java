package com.concesionario.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import com.concesionario.repository.VehiculoRepository;
import com.concesionario.model.Vehiculo;
import java.util.List;
import java.util.Comparator;
import java.util.Arrays;

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

    public String analizarYResponder(String mensajeUsuario) {
        // 1. Extraer palabras clave (más de 2 letras) del mensaje para filtrar contextualmente
        List<String> palabrasClave = Arrays.stream(mensajeUsuario.toLowerCase().replaceAll("[^a-záéíóúñ0-9\\s]", "").split("\\s+"))
                                           .filter(p -> p.length() > 2)
                                           .toList();
                                           
        List<Vehiculo> vehiculosFiltrados;
        if (!palabrasClave.isEmpty()) {
            // Unir las palabras con OR lógico (|) para la expresión regular
            String regex = String.join("|", palabrasClave);
            vehiculosFiltrados = vehiculoRepository.findByFiltroRegex(regex);
        } else {
            vehiculosFiltrados = vehiculoRepository.findByDestacadoTrue();
        }

        // Si no se encontraron resultados o la DB estaba vacía, usamos los destacados como fallback
        if (vehiculosFiltrados == null || vehiculosFiltrados.isEmpty()) {
            vehiculosFiltrados = vehiculoRepository.findByDestacadoTrue();
        }
                                           
        // 2. Calcular puntaje solo para los vehículos filtrados desde la base de datos
        List<Vehiculo> mejoresOpciones = vehiculosFiltrados.stream()
            .map(v -> {
                int score = 0;
                String descripcion = v.getDescripcion() != null ? v.getDescripcion().toLowerCase() : "";
                String marca = v.getMarca() != null ? v.getMarca().toLowerCase() : "";
                String modelo = v.getModelo() != null ? v.getModelo().toLowerCase() : "";
                String categoria = v.getCategoria() != null ? v.getCategoria().toLowerCase() : "";
                
                for (String palabra : palabrasClave) {
                    if (marca.contains(palabra) || modelo.contains(palabra)) {
                        score += 5; // Alta relevancia (Mencionó la marca o el modelo)
                    } else if (categoria.contains(palabra)) {
                        score += 3; // Media relevancia (Mencionó "SUV", "Camioneta", etc)
                    } else if (descripcion.contains(palabra)) {
                        score += 1; // Contexto en descripción (Familiar, campo, rápido, etc)
                    }
                }
                
                // Si el mensaje no coincide o no hay keywords claras, dar ventaja a los destacados
                if (score == 0) {
                     score = v.isDestacado() ? 1 : 0; 
                }
                return new VehiculoScore(v, score);
            })
            // Ordenar de mayor a menor puntaje
            .sorted(Comparator.comparingInt(VehiculoScore::score).reversed())
            // Tomar ÚNICAMENTE las 3 o 4 mejores opciones para no saturar los tokens
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

        var systemMessage = new SystemMessage("""
            Eres Dante, el asesor experto y asistente virtual de la concesionaria 'NextGen Motors'. 
            Tu meta es ser útil, persuasivo y amable.
            Si un usuario te pregunta quién eres, debes presentarte claramente diciendo que eres Dante, un asistente virtual enfocado en el apoyo de los usuarios para facilitar su traslado, movilidad y ayudarles a encontrar el vehículo ideal.
            MUY IMPORTANTE: Solo debes responder a preguntas relacionadas con nuestra concesionaria, nuestros vehículos, ventas, financiamiento, repuestos y servicios automotrices.
            
            El sistema ha filtrado estas opciones preliminares basándose en lo que pide el usuario:
            """ + inventario.toString() + """
            
            Basado EN LA DESCRIPCIÓN de estos vehículos que te pasé, piensa cuál es la mejor opción para el contexto del usuario, explícale de forma conversacional por qué, y recomiéndale el que mejor se adapte (puedes recomendar 1 o 2).
            INSTRUCCIÓN MUY IMPORTANTE: Tus respuestas DEBEN SER MUY BREVES, DIRECTAS Y CONCISAS. NUNCA excedas de 2 o 3 oraciones cortas por recomendación. NO uses lenguaje poético o excesivamente largo, ve al grano rápidamente.
            REGLA CRÍTICA: Cada vez que menciones un vehículo en tu respuesta, DEBES incluir su ID explícitamente en tu texto usando el formato exacto: [[ID: id_del_vehiculo]].
            Por ejemplo: "Te recomiendo el Toyota Land Cruiser [[ID: 64bf21...]] porque tiene gran espacio y tracción perfecta."
            """);

        var userMessage = new UserMessage(mensajeUsuario);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
