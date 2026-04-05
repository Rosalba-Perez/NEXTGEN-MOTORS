package com.concesionario.controller;

import com.concesionario.service.GeminiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotRestController {

    @Autowired
    private GeminiAIService geminiAIService;

    @PostMapping("/mensaje")
    public ResponseEntity<?> recibirMensaje(@RequestBody Map<String, String> request) {
        try {
            String mensaje = request.get("mensaje");

            // Usamos el nuevo método simplificado de tu servicio
            String respuestaIA = geminiAIService.analizarYResponder(mensaje);

            // Retornamos el JSON que tu chatbot.js ya sabe leer (data.respuesta)
            return ResponseEntity.ok(Map.of("respuesta", respuestaIA));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("respuesta", "Lo siento, Dante tuvo un error al pensar."));
        }
    }
}