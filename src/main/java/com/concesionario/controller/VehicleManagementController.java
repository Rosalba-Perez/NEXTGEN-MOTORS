package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.service.interfaces.ISupabaseStorageService;
import com.concesionario.service.interfaces.IVehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vehicles")
public class VehicleManagementController {

    private final IVehiculoService vehiculoService;
    private final ISupabaseStorageService supabaseStorageService;

    @Autowired
    public VehicleManagementController(IVehiculoService vehiculoService,
            ISupabaseStorageService supabaseStorageService) {
        this.vehiculoService = vehiculoService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        return "Admin/Nuevo";
    }

    @GetMapping("/obtener/{id}")
    @ResponseBody
    public Vehiculo obtenerVehiculoParaEdicion(@PathVariable String id) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id);
        if (vehiculo != null && vehiculo.getColores() == null) {
            vehiculo.setColores(new ArrayList<>());
        }
        return vehiculo;
    }

    @PostMapping("/editar/{id}")
    public String editarVehiculo(
            @PathVariable String id,
            @ModelAttribute Vehiculo vehiculo,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam("motor") String motor,
            @RequestParam("transmision") String transmision,
            @RequestParam("combustible") String combustible,
            @RequestParam("pasajeros") Integer pasajeros,
            @RequestParam String colores,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam("descripcion") String descripcion,
            Model model) {

        try {
            Vehiculo vehiculoExistente = vehiculoService.obtenerPorId(id);

            if (imagen != null && !imagen.isEmpty()) {
                vehiculoService.actualizarImagenVehiculo(vehiculoExistente, imagen);
            }

            if (modelo3d != null && !modelo3d.isEmpty()) {
                String urlModelo = supabaseStorageService.uploadFile(modelo3d);
                vehiculoExistente.setUrlModelo3d(urlModelo);
            }

            // ... (Rest of update logic from AdminController)
            vehiculoExistente.setMarca(vehiculo.getMarca());
            vehiculoExistente.setModelo(vehiculo.getModelo());
            vehiculoExistente.setAño(vehiculo.getAño());
            vehiculoExistente.setPrecio(vehiculo.getPrecio());
            vehiculoExistente.setCategoria(vehiculo.getCategoria());
            vehiculoExistente.setMotor(motor);
            vehiculoExistente.setTransmision(transmision);
            vehiculoExistente.setCombustible(combustible);
            vehiculoExistente.setPasajeros(pasajeros);
            vehiculoExistente.setDescripcion(descripcion);

            if (colores != null && !colores.isEmpty()) {
                List<String> listaColores = Arrays.stream(colores.split(","))
                        .map(String::trim)
                        .filter(color -> !color.isEmpty())
                        .collect(Collectors.toList());
                vehiculoExistente.setColores(listaColores);
            }

            vehiculoService.guardarVehiculo(vehiculoExistente);
            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Error al editar el vehículo: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarVehiculo(@PathVariable String id) {
        vehiculoService.eliminarVehiculo(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/guardar-anuncio")
    public String guardarAnuncio(
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int año,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam int pasajeros,
            @RequestParam String descripcion,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam MultipartFile imagen,
            Model model) throws IOException {

        try {
            Vehiculo anuncio = new Vehiculo();
            anuncio.setMarca(marca);
            anuncio.setModelo(modelo);
            anuncio.setAño(año);
            anuncio.setPrecio(precio);
            anuncio.setCategoria(categoria);
            anuncio.setMotor(motor);
            anuncio.setTransmision(transmision);
            anuncio.setCombustible(combustible);
            anuncio.setPasajeros(pasajeros);
            anuncio.setDescripcion(descripcion);
            anuncio.setDestacado(true);

            if (modelo3d != null && !modelo3d.isEmpty()) {
                String urlModelo = supabaseStorageService.uploadFile(modelo3d);
                anuncio.setUrlModelo3d(urlModelo);
            }

            vehiculoService.crearAnuncio(anuncio, imagen);
            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el anuncio: " + e.getMessage());
            return "redirect:/admin/dashboard?error=Error+al+crear+anuncio";
        }
    }

    @PostMapping("/guardar")
    public String guardarVehiculoNormal(
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int año,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam int pasajeros,
            @RequestParam String descripcion,
            @RequestParam String colores,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam MultipartFile imagen) throws IOException {

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setAño(año);
        vehiculo.setPrecio(precio);
        vehiculo.setCategoria(categoria);
        vehiculo.setMotor(motor);
        vehiculo.setTransmision(transmision);
        vehiculo.setCombustible(combustible);
        vehiculo.setPasajeros(pasajeros);
        vehiculo.setDescripcion(descripcion);
        vehiculo.setDestacado(false);

        if (modelo3d != null && !modelo3d.isEmpty()) {
            try {
                String urlModelo = supabaseStorageService.uploadFile(modelo3d);
                vehiculo.setUrlModelo3d(urlModelo);
            } catch (Exception e) {
                // Log error
            }
        }

        if (colores != null && !colores.isEmpty()) {
            List<String> listaColores = Arrays.stream(colores.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            vehiculo.setColores(listaColores);
        }

        vehiculoService.crearVehiculoNormal(vehiculo, imagen, null);
        return "redirect:/admin/dashboard";
    }
}
