package com.concesionario.controller;

import com.concesionario.model.Cita;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.service.NotificacionService;
import com.concesionario.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/appointments")
public class AppointmentController {

    private final ICitaService citaService;
    private final NotificacionService notificacionService;
    private final NotificationService notificationService;

    @Autowired
    public AppointmentController(ICitaService citaService,
            NotificacionService notificacionService,
            NotificationService notificationService) {
        this.citaService = citaService;
        this.notificacionService = notificacionService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String listarCitas(Model model) {
        List<Cita> citas = citaService.obtenerTodasLasCitas();
        model.addAttribute("citas", citas);
        model.addAttribute("numeroNotificaciones", notificacionService.contarCitasNoLeidas());
        return "Admin/CitasLista";
    }

    @ResponseBody
    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstadoCita(@PathVariable String id, @RequestParam String estado) {
        Cita cita = citaService.obtenerCitaPorId(id);
        if (cita == null)
            return "Cita no encontrada";

        cita.setEstado(estado);
        cita.setLeida(true);
        citaService.guardarCita(cita);

        if (cita.getUsuario() != null) {
            notificationService.enviarNotificacion(
                    cita.getUsuario().getId(),
                    "Actualización de Cita",
                    "El administrador cambió el estado de tu cita a: " + estado);
        }
        return "OK";
    }

    @ResponseBody
    @PostMapping("/{id}/asignar-fecha")
    public String asignarFechaCita(@PathVariable String id, @RequestParam String fecha) {
        Cita cita = citaService.obtenerCitaPorId(id);
        if (cita == null)
            return "Cita no encontrada";

        LocalDateTime fechaHora = LocalDateTime.parse(fecha.replace(" ", "T"));
        cita.setFechaAsignada(fechaHora);
        citaService.guardarCita(cita);

        if (cita.getUsuario() != null) {
            notificationService.enviarNotificacion(
                    cita.getUsuario().getId(),
                    "Cita Programada",
                    "El administrador ha programado tu cita para el: " + fecha);
        }
        return "OK";
    }

    @ResponseBody
    @GetMapping("/{id}/notas")
    public String obtenerNotasCita(@PathVariable String id) {
        Cita cita = citaService.obtenerCitaPorId(id);
        return (cita != null && cita.getNotasAdmin() != null) ? cita.getNotasAdmin() : "";
    }

    @ResponseBody
    @PostMapping("/{id}/guardar-notas")
    public String guardarNotasCita(@PathVariable String id, @RequestParam String notas) {
        Cita cita = citaService.obtenerCitaPorId(id);
        if (cita == null)
            return "Cita no encontrada";

        cita.setNotasAdmin(notas);
        citaService.guardarCita(cita);

        if (cita.getUsuario() != null) {
            notificationService.enviarNotificacion(
                    cita.getUsuario().getId(),
                    "Nueva Nota en tu Cita",
                    "Nota del administrador: " + (notas.length() > 50 ? notas.substring(0, 47) + "..." : notas));
        }
        return "OK";
    }

    @ResponseBody
    @GetMapping("/{id}/datos")
    public Map<String, Object> obtenerDatosCita(@PathVariable String id) {
        Cita cita = citaService.obtenerCitaPorId(id);
        if (cita == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");

        Map<String, Object> datos = new HashMap<>();
        datos.put("estado", cita.getEstado());
        datos.put("fechaAsignada", cita.getFechaAsignada());
        datos.put("notasAdmin", cita.getNotasAdmin());
        return datos;
    }
}
