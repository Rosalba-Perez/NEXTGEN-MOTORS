package com.concesionario.controller;

import com.concesionario.model.Administrador;
import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Usuario;
import com.concesionario.service.interfaces.IAdministradorService;
import com.concesionario.service.interfaces.ITrabajadorDetailsService;
import com.concesionario.service.interfaces.IUsuarioService;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/usuario")
public class UserProfileController {

    private final IAdministradorService administradorService;
    private final ITrabajadorDetailsService trabajadorDetailsService;
    private final IUsuarioService usuarioService;
    private final ICitaService citaService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserProfileController(IAdministradorService administradorService,
            ITrabajadorDetailsService trabajadorDetailsService,
            IUsuarioService usuarioService,
            ICitaService citaService,
            PasswordEncoder passwordEncoder) {
        this.administradorService = administradorService;
        this.trabajadorDetailsService = trabajadorDetailsService;
        this.usuarioService = usuarioService;
        this.citaService = citaService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {
        String email = SecurityUtils.getEmailFromPrincipal(principal);

        try {
            Administrador administrador = administradorService.findByCorreoAdmin(email);
            if (administrador != null && administrador.tieneRol(Rol.ADMINISTRADOR)) {
                return "redirect:/admin/dashboard";
            }
        } catch (Exception e) {
            // Not an admin
        }

        try {
            Trabajador trabajador = trabajadorDetailsService.findByCorreo(email);
            if (trabajador != null) {
                if (trabajador.tieneRol(Rol.TRB_GESTOR))
                    return "redirect:/perfil_gestor";
                if (trabajador.tieneRol(Rol.TRB_ANALISIS))
                    return "redirect:/perfil_analisis";
                if (trabajador.tieneRol(Rol.TRB_ASESOR))
                    return "redirect:/perfil_asesor";
                if (trabajador.tieneRol(Rol.TRABAJADOR))
                    return "redirect:/trabajador/dashboard";
            }
        } catch (Exception e) {
            // Not a worker
        }

        Usuario usuario = usuarioService.findByCorreoUser(email);
        if (usuario.getIdentificacionUser() == null || usuario.getIdentificacionUser().isEmpty()) {
            return "redirect:/usuario/completar-perfil";
        }

        model.addAttribute("nombreUsuario", usuario.getNombreUser());
        model.addAttribute("citas", citaService.obtenerCitasPorUsuarioId(usuario.getId()));
        return "usuario/perfil";
    }

    @GetMapping("/completar-perfil")
    public String mostrarCompletarPerfil(Principal principal, Model model) {
        Usuario usuario = usuarioService.findByCorreoUser(SecurityUtils.getEmailFromPrincipal(principal));
        model.addAttribute("usuario", usuario);
        return "usuario/completar-perfil";
    }

    @PostMapping("/completar-perfil")
    public String guardarPerfilCompleto(@RequestParam String identificacion,
            @RequestParam String password,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.findByCorreoUser(SecurityUtils.getEmailFromPrincipal(principal));
            usuario.setIdentificacionUser(identificacion);
            usuario.setPasswordUser(passwordEncoder.encode(password));
            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("success", "Perfil completado exitosamente.");
            return "redirect:/usuario/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al completar el perfil: " + e.getMessage());
            return "redirect:/usuario/completar-perfil";
        }
    }
}
