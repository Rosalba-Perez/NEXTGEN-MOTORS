package com.concesionario.controller;

import com.concesionario.model.Trabajador;
import com.concesionario.service.interfaces.ITrabajadorDetailsService;
import com.concesionario.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AsesorProfileController {

    private final ITrabajadorDetailsService trabajadorDetailsService;

    @Autowired
    public AsesorProfileController(ITrabajadorDetailsService trabajadorDetailsService) {
        this.trabajadorDetailsService = trabajadorDetailsService;
    }

    @GetMapping("/perfil_asesor")
    public String perfilAsesor(Model model, Principal principal) {
        String nombreUsuario = "Asesor";
        if (principal != null) {
            Trabajador trabajador = trabajadorDetailsService
                    .findByCorreo(SecurityUtils.getEmailFromPrincipal(principal));
            if (trabajador != null) {
                nombreUsuario = trabajador.getNombre();
                model.addAttribute("asesorId", trabajador.getId());
            }
        }
        model.addAttribute("nombreUsuario", nombreUsuario);
        return "perfil_asesor";
    }
}
