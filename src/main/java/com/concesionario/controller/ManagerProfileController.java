package com.concesionario.controller;

import com.concesionario.model.Trabajador;
import com.concesionario.model.Vehiculo;
import com.concesionario.service.interfaces.ITrabajadorService;
import com.concesionario.service.interfaces.IVehiculoService;
import com.concesionario.service.interfaces.IProspectoService;
import com.concesionario.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
public class ManagerProfileController {

    private final IVehiculoService vehiculoService;
    private final ITrabajadorService trabajadorService;
    private final IProspectoService prospectoService;

    @Autowired
    public ManagerProfileController(IVehiculoService vehiculoService,
            ITrabajadorService trabajadorService,
            IProspectoService prospectoService) {
        this.vehiculoService = vehiculoService;
        this.trabajadorService = trabajadorService;
        this.prospectoService = prospectoService;
    }

    @GetMapping("/perfil_gestor")
    public String perfilGestor(Model model, Authentication authentication) {
        List<Vehiculo> vehiculos = vehiculoService.obtenerVehiculosNormales();
        List<Vehiculo> anuncios = vehiculoService.obtenerDestacados();

        String nombreUsuario = "Gestor";
        if (authentication != null) {
            Trabajador trabajador = trabajadorService.findByCorreo(SecurityUtils.getEmailFromPrincipal(authentication));
            if (trabajador != null)
                nombreUsuario = trabajador.getNombre();
        }

        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("anuncios", anuncios);
        model.addAttribute("totalVehiculos", vehiculos.size());
        model.addAttribute("totalAnuncios", anuncios.size());

        return "perfil_gestor";
    }

    @GetMapping("/gestor/descargar-analisis-rendimiento")
    public ResponseEntity<InputStreamResource> descargarAnalisisRendimiento() {
        try {
            ByteArrayInputStream in = prospectoService.generarReporteRendimientoMensual();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=Analisis_Rendimiento_Asesores.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
