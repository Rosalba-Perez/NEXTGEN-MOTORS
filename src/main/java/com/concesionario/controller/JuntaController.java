package com.concesionario.controller;

import com.concesionario.model.Junta;
import com.concesionario.service.interfaces.IJuntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/juntas")
public class JuntaController {

    private final IJuntaService juntaService;

    @Autowired
    public JuntaController(IJuntaService juntaService) {
        this.juntaService = juntaService;
    }

    @PostMapping("/crear")
    public String crearJunta(@ModelAttribute Junta junta,
            @RequestParam(value = "participantesIds", required = false) List<String> participantesIds,
            RedirectAttributes redirectAttributes) {
        try {
            junta.setParticipantesIds(participantesIds);
            juntaService.crearJunta(junta);
            redirectAttributes.addFlashAttribute("success", "Junta programada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al programar la junta: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
