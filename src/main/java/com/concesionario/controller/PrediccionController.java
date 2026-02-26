package com.concesionario.controller;

import com.concesionario.model.Usuario;
import com.concesionario.service.interfaces.IPrediccionService;
import com.concesionario.service.interfaces.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PrediccionController {

    private final IPrediccionService prediccionService;
    private final IUsuarioService usuarioService;

    @Autowired
    public PrediccionController(IPrediccionService prediccionService,
            IUsuarioService usuarioService) {
        this.prediccionService = prediccionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cargar-usuarios")
    @ResponseBody
    public List<Usuario> cargarUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        for (Usuario usuario : usuarios) {
            usuarioService.calcularDatosDesdeCitas(usuario);
        }
        return usuarios;
    }

    @PostMapping("/predecir-usuario")
    @ResponseBody
    public Map<String, Object> predecirUsuario(@RequestParam String usuarioId) {
        Map<String, Object> response = new HashMap<>();
        Usuario usuario = usuarioService.findById(usuarioId);
        if (usuario != null) {
            usuarioService.calcularDatosDesdeCitas(usuario);
            prediccionService.aplicarPrediccionYActualizar(usuario);
            usuarioService.save(usuario);

            response.put("success", true);
            response.put("usuario", usuario);
        } else {
            response.put("success", false);
            response.put("error", "Usuario no encontrado");
        }
        return response;
    }

    @GetMapping("/prediccion")
    public String mostrarPrediccion(@RequestParam(required = false) Boolean cargarUsuarios, Model model) {
        if (cargarUsuarios != null && cargarUsuarios) {
            List<Usuario> usuarios = usuarioService.findAll();
            for (Usuario usuario : usuarios) {
                usuarioService.calcularDatosDesdeCitas(usuario);
            }
            model.addAttribute("usuarios", usuarios);
        }
        return "prediccion";
    }

    @PostMapping("/predecir-usuario-old")
    public String predecirUsuarioOld(@RequestParam String usuarioId, Model model) {
        Usuario usuario = usuarioService.findById(usuarioId);
        if (usuario != null) {
            usuarioService.calcularDatosDesdeCitas(usuario);
            prediccionService.aplicarPrediccionYActualizar(usuario);
            usuarioService.save(usuario);
            model.addAttribute("usuarioSeleccionado", usuario);
        }

        List<Usuario> usuarios = usuarioService.findAll();
        for (Usuario u : usuarios) {
            usuarioService.calcularDatosDesdeCitas(u);
        }
        model.addAttribute("usuarios", usuarios);
        return "prediccion";
    }
}