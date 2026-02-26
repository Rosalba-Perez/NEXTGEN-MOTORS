package com.concesionario.controller;

import com.concesionario.service.interfaces.*;
import com.concesionario.service.interfaces.IAdminNotificationService;
import com.concesionario.service.interfaces.ITrabajadorService;
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
    private final IAdminNotificationService adminNotificationService;
    private final ITrabajadorService trabajadorService;
    private final com.concesionario.service.interfaces.ISedeService sedeService;
    private final com.concesionario.service.interfaces.IJuntaService juntaService;

    @Autowired
    public AdminDashboardController(IVehiculoService vehiculoService,
            ICitaService citaService,
            IUsuarioService usuarioService,
            IAdminNotificationService adminNotificationService,
            ITrabajadorService trabajadorService,
            com.concesionario.service.interfaces.ISedeService sedeService,
            com.concesionario.service.interfaces.IJuntaService juntaService) {
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.usuarioService = usuarioService;
        this.adminNotificationService = adminNotificationService;
        this.trabajadorService = trabajadorService;
        this.sedeService = sedeService;
        this.juntaService = juntaService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Estadísticas
        model.addAttribute("totalCitas", citaService.contarCitas());
        model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
        model.addAttribute("totalVehiculos", vehiculoService.contarTodosVehiculos());

        // Listados
        model.addAttribute("vehiculos", vehiculoService.obtenerVehiculosNormales());
        model.addAttribute("anuncios", vehiculoService.obtenerDestacados());
        model.addAttribute("citas", citaService.obtenerCitasPendientes());
        model.addAttribute("trabajadores", trabajadorService.listarActivos());
        model.addAttribute("sedes", sedeService.listarTodas());
        model.addAttribute("juntas", juntaService.listarTodas());

        // Notificaciones
        model.addAttribute("numeroNotificaciones", adminNotificationService.contarCitasNoLeidas());
        model.addAttribute("citasNoLeidas", adminNotificationService.obtenerCitasNoLeidas());

        return "admin/dashboard";
    }
}
