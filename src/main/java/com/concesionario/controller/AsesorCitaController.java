package com.concesionario.controller;

import com.concesionario.model.Cita;
import com.concesionario.model.Trabajador;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.service.interfaces.IPushNotificationService;
import com.concesionario.service.interfaces.ITrabajadorDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/asesor/citas")
public class AsesorCitaController {

    private final ICitaService citaService;
    private final ITrabajadorDetailsService trabajadorDetailsService;
    private final IPushNotificationService pushNotificationService;

    @Autowired
    public AsesorCitaController(ICitaService citaService,
            ITrabajadorDetailsService trabajadorDetailsService,
            IPushNotificationService pushNotificationService) {
        this.citaService = citaService;
        this.trabajadorDetailsService = trabajadorDetailsService;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping
    public List<Map<String, Object>> obtenerCitasAsesor(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            List<Cita> citas = citaService.obtenerCitasPorTrabajador(asesor.getId());

            return citas.stream().map(cita -> {
                Map<String, Object> citaMap = new HashMap<>();

                String nombreCompleto = "";
                if (cita.getUsuario() != null) {
                    nombreCompleto = (cita.getUsuario().getNombre() != null ? cita.getUsuario().getNombre() : "") + " "
                            +
                            (cita.getUsuario().getApellido() != null ? cita.getUsuario().getApellido() : "");
                }
                citaMap.put("id", cita.getId());
                citaMap.put("cliente", nombreCompleto.trim());
                citaMap.put("tipo", cita.getTipo() != null ? cita.getTipo() : "No especificado");

                String vehiculo = "No especificado";
                if (cita.getVehiculo() != null && cita.getVehiculo().getModelo() != null) {
                    vehiculo = cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo();
                } else if (cita.getNombreVehiculo() != null && !cita.getNombreVehiculo().isEmpty()) {
                    vehiculo = cita.getNombreVehiculo();
                }
                citaMap.put("vehiculo", vehiculo);

                citaMap.put("fechaSolicitud", cita.getFechaCreacion() != null ? cita.getFechaCreacion() : "");
                citaMap.put("fechaAsignada", cita.getFechaAsignada() != null ? cita.getFechaAsignada() : "");

                citaMap.put("comentario", cita.getComentario() != null ? cita.getComentario() : "Sin comentario");
                citaMap.put("estado", cita.getEstado() != null ? cita.getEstado() : "Pendiente");
                citaMap.put("notasAdmin", cita.getNotasAdmin() != null ? cita.getNotasAdmin() : "Sin notas");

                return citaMap;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener citas: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstadoCita(@PathVariable String id, @RequestParam String estado) {
        try {
            Cita cita = citaService.obtenerCitaPorId(id);
            if (cita == null)
                throw new RuntimeException("Cita no encontrada");

            cita.setEstado(estado);
            citaService.guardarCita(cita);

            if (cita.getUsuario() != null) {
                String nombreVehiculo = "tu vehículo de interés";
                if (cita.getVehiculo() != null) {
                    nombreVehiculo = cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo();
                } else if (cita.getNombreVehiculo() != null) {
                    nombreVehiculo = cita.getNombreVehiculo();
                }

                String titulo = "";
                String mensaje = "";

                switch (estado) {
                    case "Aprobada":
                        titulo = "¡Cita Confirmada! 🚗✅";
                        mensaje = "Buenas noticias. Tu cita en NextGen Motors para ver el " + nombreVehiculo
                                + " ha sido aprobada. ¡Te esperamos!";
                        break;
                    case "Rechazada":
                        titulo = "Actualización de Cita 📅";
                        mensaje = "Lo sentimos, tu solicitud para el " + nombreVehiculo
                                + " no pudo ser procesada en este horario. Por favor revisa los detalles.";
                        break;
                    case "Completada":
                        titulo = "¡Gracias por visitarnos! 🤝";
                        mensaje = "Fue un placer atenderte. Esperamos que hayas disfrutado tu experiencia con el "
                                + nombreVehiculo + ".";
                        break;
                    default:
                        titulo = "Estado de Cita Actualizado 🔄";
                        mensaje = "Tu cita para el " + nombreVehiculo + " ahora está: " + estado + ".";
                }

                pushNotificationService.enviarNotificacion(cita.getUsuario().getId(), titulo, mensaje);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/asignar-fecha")
    public ResponseEntity<?> asignarFechaCita(@PathVariable String id,
            @RequestParam String fecha,
            @RequestParam String hora) {
        try {
            Cita cita = citaService.obtenerCitaPorId(id);
            if (cita == null)
                throw new RuntimeException("Cita no encontrada");

            LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
            cita.setFechaAsignada(fechaHora);
            citaService.guardarCita(cita);

            if (cita.getUsuario() != null) {
                String nombreVehiculo = "tu vehículo";
                if (cita.getVehiculo() != null) {
                    nombreVehiculo = cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo();
                }

                pushNotificationService.enviarNotificacion(
                        cita.getUsuario().getId(),
                        "¡Fecha Asignada! 🗓️📍",
                        "Hemos programado tu cita para ver el " + nombreVehiculo + " el día " + fecha + " a las " + hora
                                + ". ¡Nos vemos pronto!");
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/guardar-notas")
    public ResponseEntity<?> guardarNotasCita(@PathVariable String id, @RequestParam String notas) {
        try {
            Cita cita = citaService.obtenerCitaPorId(id);
            if (cita == null)
                throw new RuntimeException("Cita no encontrada");

            cita.setNotasAdmin(notas);
            citaService.guardarCita(cita);

            if (cita.getUsuario() != null) {
                pushNotificationService.enviarNotificacion(
                        cita.getUsuario().getId(),
                        "Nuevo Mensaje del Asesor 💬",
                        "Tienes un nuevo comentario sobre tu cita: \""
                                + (notas.length() > 50 ? notas.substring(0, 47) + "..." : notas) + "\"");
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/datos")
    public ResponseEntity<?> obtenerDatosCita(@PathVariable String id) {
        try {
            Cita cita = citaService.obtenerCitaPorId(id);
            if (cita == null)
                throw new RuntimeException("Cita no encontrada");

            Map<String, Object> citaMap = new HashMap<>();
            citaMap.put("id", cita.getId());
            citaMap.put("fechaAsignada", cita.getFechaAsignada());
            citaMap.put("fechaSolicitud", cita.getFechaCreacion());
            citaMap.put("estado", cita.getEstado());
            citaMap.put("notasAdmin", cita.getNotasAdmin());

            if (cita.getUsuario() != null) {
                citaMap.put("cliente", cita.getUsuario().getNombre() + " " + cita.getUsuario().getApellido());
            } else {
                citaMap.put("cliente", (cita.getNombres() != null ? cita.getNombres() : "") + " "
                        + (cita.getApellidos() != null ? cita.getApellidos() : ""));
            }

            return ResponseEntity.ok(citaMap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
