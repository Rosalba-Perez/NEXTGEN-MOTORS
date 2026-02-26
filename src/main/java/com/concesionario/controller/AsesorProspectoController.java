package com.concesionario.controller;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Trabajador;
import com.concesionario.service.interfaces.IProspectoService;
import com.concesionario.service.interfaces.ITrabajadorDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/asesor/prospectos")
public class AsesorProspectoController {

    private final IProspectoService prospectoService;
    private final ITrabajadorDetailsService trabajadorDetailsService;

    @Autowired
    public AsesorProspectoController(IProspectoService prospectoService,
            ITrabajadorDetailsService trabajadorDetailsService) {
        this.prospectoService = prospectoService;
        this.trabajadorDetailsService = trabajadorDetailsService;
    }

    @GetMapping
    public List<ProspectoDTO> obtenerProspectos(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            return prospectoService.obtenerProspectosParaAsesor(asesor.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener prospectos: " + e.getMessage());
        }
    }

    @PostMapping("/nuevo")
    public ResponseEntity<?> registrarNuevoProspecto(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(required = false) String correo,
            @RequestParam String telefono,
            @RequestParam String vehiculoInteres,
            @RequestParam(required = false) String observaciones,
            Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());

            prospectoService.registrarProspectoManual(
                    nombre, apellido, correo, telefono, vehiculoInteres, asesor.getId(), observaciones);

            return ResponseEntity.ok(Map.of("success", true, "message", "Prospecto registrado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/contactar")
    public ResponseEntity<?> marcarComoContactado(@RequestParam String prospectoId) {
        try {
            prospectoService.cambiarEstadoContactado(prospectoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    @PostMapping("/cambiar-estado")
    public ResponseEntity<?> cambiarEstadoProspecto(@RequestParam String prospectoId,
            @RequestParam String nuevoEstado) {
        try {
            prospectoService.actualizarEstadoProspecto(prospectoId, nuevoEstado);
            return ResponseEntity.ok(Map.of("success", true, "message", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}
