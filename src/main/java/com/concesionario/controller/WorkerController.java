package com.concesionario.controller;

import com.concesionario.model.Trabajador;
import com.concesionario.model.Rol;
import com.concesionario.repository.TrabajadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/workers")
public class WorkerController {

    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public WorkerController(TrabajadorRepository trabajadorRepository, PasswordEncoder passwordEncoder) {
        this.trabajadorRepository = trabajadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public String registroTrabajador(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("correo") String correo,
            @RequestParam("identificacion") String identificacion,
            @RequestParam("password") String password,
            @RequestParam("horaInicio") @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
            @RequestParam("horaFin") @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin,
            @RequestParam(value = "diasTrabajo", required = false) List<String> diasTrabajo,
            @RequestParam(value = "roles", required = false) List<String> rolesStrings,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (diasTrabajo == null || diasTrabajo.isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos un día de trabajo");
            return manejarErrorRegistro(model, nombre, apellido, correo, identificacion, horaInicio, horaFin);
        }

        if (rolesStrings == null || rolesStrings.isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos un rol para el trabajador");
            return manejarErrorRegistro(model, nombre, apellido, correo, identificacion, horaInicio, horaFin);
        }

        try {
            Trabajador trabajador = new Trabajador();
            trabajador.setNombre(nombre);
            trabajador.setApellido(apellido);
            trabajador.setCorreo(correo);
            trabajador.setIdentificacion(identificacion);
            trabajador.setPassword(passwordEncoder.encode(password));
            trabajador.setHoraInicioTrabajo(horaInicio);
            trabajador.setHoraFinTrabajo(horaFin);
            trabajador.setDiasTrabajo(diasTrabajo);

            List<Rol> roles = rolesStrings.stream()
                    .map(Rol::valueOf)
                    .collect(Collectors.toList());

            trabajador.setRoles(roles);
            trabajadorRepository.save(trabajador);

            redirectAttributes.addFlashAttribute("success", "Trabajador registrado exitosamente");
            return "redirect:/admin/dashboard";

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Error: El correo o identificación ya existen");
            return manejarErrorRegistro(model, nombre, apellido, correo, identificacion, horaInicio, horaFin);
        } catch (Exception e) {
            model.addAttribute("error", "Error inesperado: " + e.getMessage());
            return manejarErrorRegistro(model, nombre, apellido, correo, identificacion, horaInicio, horaFin);
        }
    }

    private String manejarErrorRegistro(Model model, String nombre, String apellido,
            String correo, String identificacion,
            LocalTime horaInicio, LocalTime horaFin) {
        model.addAttribute("nombre", nombre);
        model.addAttribute("apellido", apellido);
        model.addAttribute("correo", correo);
        model.addAttribute("identificacion", identificacion);
        model.addAttribute("horaInicio", horaInicio);
        model.addAttribute("horaFin", horaFin);
        return "admin/dashboard";
    }

    @PostMapping("/despedir/{id}")
    @ResponseBody
    public String despedirTrabajador(@PathVariable String id) {
        try {
            trabajadorRepository.deleteById(id);
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
