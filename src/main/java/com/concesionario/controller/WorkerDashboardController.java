package com.concesionario.controller;

import com.concesionario.service.interfaces.IAdminNotificationService;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.service.interfaces.IVehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trabajador")
public class WorkerDashboardController {

    private final IVehiculoService vehiculoService;
    private final ICitaService citaService;
    private final IAdminNotificationService adminNotificationService;

    @Autowired
    public WorkerDashboardController(IVehiculoService vehiculoService,
            ICitaService citaService,
            IAdminNotificationService adminNotificationService) {
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.adminNotificationService = adminNotificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Essential data for workers
        model.addAttribute("citas", citaService.obtenerCitasPendientes());
        model.addAttribute("vehiculos", vehiculoService.obtenerVehiculosNormales());
        model.addAttribute("numeroNotificaciones", adminNotificationService.contarCitasNoLeidas());

        return "trabajador/dashboard";
    }
}
