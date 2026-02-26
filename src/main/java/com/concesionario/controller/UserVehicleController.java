package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.service.interfaces.IVehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UserVehicleController {

    private final IVehiculoService vehiculoService;

    @Autowired
    public UserVehicleController(IVehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/usuario/Inicio")
    public String Inicio(Model model) {
        List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();

        Map<String, List<Vehiculo>> vehiculosPorCategoria = vehiculos.stream()
                .filter(v -> v.getCategoria() != null)
                .collect(Collectors.groupingBy(Vehiculo::getCategoria));

        List<Vehiculo> vehiculosDestacados = Optional.ofNullable(vehiculoService.obtenerDestacados())
                .orElse(Collections.emptyList());

        model.addAttribute("categorias", vehiculosPorCategoria.keySet());
        model.addAttribute("vehiculosPorCategoria", vehiculosPorCategoria);
        model.addAttribute("destacados", vehiculosDestacados);

        return "index";
    }
}
