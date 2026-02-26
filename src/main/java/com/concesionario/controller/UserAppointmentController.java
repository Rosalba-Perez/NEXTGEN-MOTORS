package com.concesionario.controller;

import com.concesionario.model.*;
import com.concesionario.service.interfaces.*;
import com.concesionario.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/usuario")
public class UserAppointmentController {

    private final ICitaService citaService;
    private final ITrabajadorService trabajadorService;
    private final IVehiculoService vehiculoService;
    private final IUsuarioService usuarioService;

    @Autowired
    public UserAppointmentController(ICitaService citaService,
            ITrabajadorService trabajadorService,
            IVehiculoService vehiculoService,
            IUsuarioService usuarioService) {
        this.citaService = citaService;
        this.trabajadorService = trabajadorService;
        this.vehiculoService = vehiculoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/agendamiento")
    public String mostrarAgendamiento() {
        return "agendamiento";
    }

    @GetMapping("/cita")
    public String mostrarFormularioCita(
            @RequestParam(required = false) String vehiculoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String color,
            Principal principal,
            Model model) {

        Usuario usuario = usuarioService.findByCorreoUser(SecurityUtils.getEmailFromPrincipal(principal));
        Cita cita = new Cita();
        cita.setNombres(usuario.getNombreUser());
        cita.setApellidos(usuario.getApellidoUser());
        cita.setCedula(usuario.getIdentificacionUser());
        cita.setCorreoElectronico(usuario.getCorreoUser());

        if (vehiculoId != null) {
            Vehiculo vehiculo = vehiculoService.obtenerPorId(vehiculoId);
            if (vehiculo != null) {
                cita.setVehiculoId(vehiculo.getId());
                cita.setNombreVehiculo(vehiculo.getMarca() + " " + vehiculo.getModelo());
                cita.setColorVehiculo(color);
                model.addAttribute("coloresVehiculo",
                        vehiculo.getColores() != null ? vehiculo.getColores() : Collections.emptyList());
            }
        } else if (tipo != null) {
            cita.setTipo(tipo);
        } else {
            cita.setTipo("Otros");
        }

        model.addAttribute("trabajadores", trabajadorService.listarPorRol(Rol.TRB_ASESOR));
        model.addAttribute("cita", cita);
        model.addAttribute("vehiculos", vehiculoService.obtenerTodos());
        return "cita";
    }

    @PostMapping("/cita/guardar")
    public String guardarCita(@ModelAttribute Cita cita,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Trabajador trabajador = trabajadorService.obtenerPorId(cita.getTrabajadorId());
            if (trabajador == null)
                throw new RuntimeException("Trabajador no encontrado");

            if (!esDiaLaboral(trabajador, cita.getFechaCita())) {
                redirectAttributes.addFlashAttribute("error", "El trabajador no labora ese día");
                return "redirect:/usuario/cita";
            }

            if (!citaService.isHoraDisponible(cita.getTrabajadorId(), cita.getFechaCita(), cita.getHoraCita())) {
                redirectAttributes.addFlashAttribute("error", "La hora seleccionada ya está ocupada");
                return "redirect:/usuario/cita";
            }

            Usuario usuario = usuarioService.findByCorreoUser(SecurityUtils.getEmailFromPrincipal(principal));
            if (!validarDatosInmutables(cita, usuario)) {
                redirectAttributes.addFlashAttribute("error", "No puedes modificar tus datos personales");
                return "redirect:/usuario/cita";
            }

            Vehiculo vehiculo = cita.getVehiculoId() != null ? vehiculoService.obtenerPorId(cita.getVehiculoId())
                    : null;

            if ("Otros".equals(cita.getTipo())) {
                citaService.guardarCitaSimple(cita, usuario, vehiculo);
            } else {
                citaService.crearCitaConEmbedding(cita, usuario, vehiculo);
            }

            return "redirect:/usuario/perfil?success=Cita+agendada+exitosamente";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agendar: " + e.getMessage());
            return "redirect:/usuario/cita";
        }
    }

    @GetMapping("/mis-citas")
    public String verMisCitas(Model model, Principal principal) {
        Usuario usuario = usuarioService.findByCorreoUser(SecurityUtils.getEmailFromPrincipal(principal));
        model.addAttribute("citas", citaService.obtenerCitasPorUsuarioId(usuario.getId()));
        model.addAttribute("nombreUsuario", usuario.getNombreUser() + " " + usuario.getApellidoUser());
        return "Usuario/MisCitas";
    }

    @GetMapping("/check-disponibilidad")
    public ResponseEntity<?> checkDisponibilidad(@RequestParam String trabajadorId, @RequestParam String fecha,
            @RequestParam String hora) {
        boolean disponible = citaService.isHoraDisponible(trabajadorId, fecha, hora);
        Map<String, Object> response = new HashMap<>();
        response.put("disponible", disponible);
        if (!disponible)
            response.put("mensaje", "La hora ya está ocupada");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/horas-ocupadas")
    public ResponseEntity<?> getHorasOcupadas(@RequestParam String trabajadorId, @RequestParam String fecha) {
        return ResponseEntity.ok(citaService.getHorasOcupadas(trabajadorId, fecha));
    }

    @GetMapping("/check-dia-trabajador")
    public ResponseEntity<?> verificarDiaTrabajador(@RequestParam String trabajadorId, @RequestParam String fecha) {
        Map<String, Object> response = new HashMap<>();
        try {
            Trabajador trabajador = trabajadorService.obtenerPorId(trabajadorId);
            String diaEnEspanol = convertirDiaEspanol(LocalDate.parse(fecha).getDayOfWeek());
            boolean diaValido = trabajador.getDiasTrabajo().contains(diaEnEspanol);
            response.put("valido", diaValido);
            if (!diaValido)
                response.put("mensaje", "El trabajador no labora los " + diaEnEspanol);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    private boolean esDiaLaboral(Trabajador trabajador, String fechaStr) {
        try {
            return trabajador.getDiasTrabajo().contains(convertirDiaEspanol(LocalDate.parse(fechaStr).getDayOfWeek()));
        } catch (Exception e) {
            return false;
        }
    }

    private String convertirDiaEspanol(DayOfWeek dia) {
        switch (dia) {
            case MONDAY:
                return "LUNES";
            case TUESDAY:
                return "MARTES";
            case WEDNESDAY:
                return "MIERCOLES";
            case THURSDAY:
                return "JUEVES";
            case FRIDAY:
                return "VIERNES";
            case SATURDAY:
                return "SABADO";
            case SUNDAY:
                return "DOMINGO";
            default:
                return "";
        }
    }

    private boolean validarDatosInmutables(Cita cita, Usuario usuario) {
        return usuario.getNombreUser().equals(cita.getNombres()) &&
                usuario.getApellidoUser().equals(cita.getApellidos()) &&
                usuario.getIdentificacionUser().equals(cita.getCedula()) &&
                usuario.getCorreoUser().equals(cita.getCorreoElectronico());
    }
}
