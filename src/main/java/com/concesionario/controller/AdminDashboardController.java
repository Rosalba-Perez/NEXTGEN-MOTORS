package com.concesionario.controller;

import com.concesionario.model.Cita;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.service.interfaces.IUsuarioService;
import com.concesionario.service.interfaces.IVehiculoService;
import com.concesionario.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final IVehiculoService vehiculoService;
    private final ICitaService citaService;
    private final IUsuarioService usuarioService;
    private final NotificacionService notificacionService;
    private final TrabajadorRepository trabajadorRepository;

    @Autowired
    public AdminDashboardController(IVehiculoService vehiculoService,
            ICitaService citaService,
            IUsuarioService usuarioService,
            NotificacionService notificacionService,
            TrabajadorRepository trabajadorRepository) {
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
        this.trabajadorRepository = trabajadorRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Estadísticas
        model.addAttribute("totalCitas", citaService.contarTodasLasCitas());
        model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
        model.addAttribute("totalVehiculos", vehiculoService.contarTodosVehiculos());

        // Listados
        model.addAttribute("vehiculos", vehiculoService.obtenerVehiculosNormales());
        model.addAttribute("anuncios", vehiculoService.obtenerDestacados());
        model.addAttribute("citas", citaService.obtenerCitasPendientes());
        model.addAttribute("trabajadores", trabajadorRepository.findAll());

        // Notificaciones
        model.addAttribute("numeroNotificaciones", notificacionService.contarCitasNoLeidas());
        model.addAttribute("citasNoLeidas", notificacionService.obtenerCitasNoLeidas());

        return "admin/dashboard";
    }
}
