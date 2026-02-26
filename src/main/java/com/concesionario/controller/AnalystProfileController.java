package com.concesionario.controller;

import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Usuario;
import com.concesionario.service.interfaces.*;
import com.concesionario.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class AnalystProfileController {

    private final IUsuarioService usuarioService;
    private final IVehiculoService vehiculoService;
    private final ICitaService citaService;
    private final ITrabajadorService trabajadorService;
    private final IPrediccionService prediccionService;
    private final IReporteService reporteService;

    @Autowired
    public AnalystProfileController(IUsuarioService usuarioService,
            IVehiculoService vehiculoService,
            ICitaService citaService,
            ITrabajadorService trabajadorService,
            IPrediccionService prediccionService,
            IReporteService reporteService) {
        this.usuarioService = usuarioService;
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.trabajadorService = trabajadorService;
        this.prediccionService = prediccionService;
        this.reporteService = reporteService;
    }

    @GetMapping("/perfil_analisis")
    public String perfilAnalisis(Model model, Authentication authentication) {
        String nombreUsuario = "Analista";
        if (authentication != null) {
            Trabajador trabajador = trabajadorService.findByCorreo(SecurityUtils.getEmailFromPrincipal(authentication));
            if (trabajador != null)
                nombreUsuario = trabajador.getNombre();
        }

        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("totalClientes", usuarioService.contarUsuarios());
        model.addAttribute("totalVehiculos", vehiculoService.obtenerTodos().size()); // Using size since count() might
                                                                                     // not be exact in storage
        model.addAttribute("totalCitas", citaService.contarCitas());
        model.addAttribute("totalTrabajadores", trabajadorService.contarTrabajadores());

        return "perfil_analisis";
    }

    @GetMapping("/gestor/descargar-reporte-potenciales")
    public ResponseEntity<InputStreamResource> descargarReportePotenciales() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            List<Usuario> usuariosProcesados = usuarios.stream()
                    .map(prediccionService::aplicarPrediccionYActualizar)
                    .filter(u -> "Si".equals(u.getClientePotencial()))
                    .toList();

            ByteArrayInputStream in = reporteService.generarReportePotenciales(usuariosProcesados);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=Reporte_Usuarios_Potenciales.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            throw new RuntimeException("Error al generar reporte", e);
        }
    }
}
