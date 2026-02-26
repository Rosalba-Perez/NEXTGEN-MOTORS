package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.service.interfaces.IVehiculoRecomendacionService;
import com.concesionario.service.interfaces.IVehiculoService;
import com.concesionario.dto.RecomendacionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PublicVehicleController {

    private final IVehiculoService vehiculoService;
    private final ICitaService citaService;
    private final IVehiculoRecomendacionService recomendacionService;

    @Autowired
    public PublicVehicleController(IVehiculoService vehiculoService,
            ICitaService citaService,
            IVehiculoRecomendacionService recomendacionService) {
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.recomendacionService = recomendacionService;
    }

    @GetMapping("/vehiculos")
    public String mostrarVehiculos(Model model) {
        List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();
        Map<String, List<Vehiculo>> vehiculosPorCategoria = vehiculos.stream()
                .filter(v -> v.getCategoria() != null && !v.getCategoria().isEmpty())
                .collect(Collectors.groupingBy(Vehiculo::getCategoria));

        model.addAttribute("categorias", vehiculosPorCategoria.keySet());
        model.addAttribute("vehiculosPorCategoria", vehiculosPorCategoria);
        return "vehiculos";
    }

    @GetMapping("/fragments/chatbot")
    public String chatbot() {
        return "chatbot";
    }

    @PostMapping("/api/chatbot/mensaje")
    @ResponseBody
    public ResponseEntity<ChatbotResponse> procesarMensajeChatbot(@RequestBody ChatbotRequest request) {
        try {
            RecomendacionResponse recomendacion = recomendacionService.procesarRecomendacion(request.getMensaje());
            ChatbotResponse response = new ChatbotResponse();
            response.setRespuesta(recomendacion.getRespuesta());
            response.setVehiculosRecomendados(recomendacion.getVehiculosRecomendados());
            response.setTimestamp(java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatbotResponse errorResponse = new ChatbotResponse();
            errorResponse.setRespuesta("Error en el chatbot.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/vehiculos/explorar/{id}")
    public String explorarVehiculo(@PathVariable String id, Model model) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id);
        if (vehiculo == null)
            return "redirect:/vehiculos";
        model.addAttribute("vehiculo", vehiculo);
        return "explorar-vehiculo";
    }

    @GetMapping("/")
    public String redirectToInicio() {
        return "redirect:/usuario/Inicio";
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }

    @GetMapping("/garantias")
    public String garantias() {
        return "garantias";
    }

    @GetMapping("/credito")
    public String credito() {
        return "credito";
    }

    @GetMapping("/cookies")
    public String cookies() {
        return "cookies";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }

    @GetMapping("/ubicaciones")
    public String ubicaciones() {
        return "ubicaciones";
    }

    public static class ChatbotRequest {
        private String mensaje;

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }

    public static class ChatbotResponse {
        private String respuesta;
        private List<Vehiculo> vehiculosRecomendados;
        private String timestamp;

        public String getRespuesta() {
            return respuesta;
        }

        public void setRespuesta(String respuesta) {
            this.respuesta = respuesta;
        }

        public List<Vehiculo> getVehiculosRecomendados() {
            return vehiculosRecomendados;
        }

        public void setVehiculosRecomendados(List<Vehiculo> vehiculosRecomendados) {
            this.vehiculosRecomendados = vehiculosRecomendados;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
