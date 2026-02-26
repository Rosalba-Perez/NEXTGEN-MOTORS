package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.service.interfaces.ISupabaseStorageService;
import com.concesionario.service.interfaces.IVehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gestor")
public class ManagerVehicleController {

    private final IVehiculoService vehiculoService;
    private final ISupabaseStorageService supabaseStorageService;

    @Autowired
    public ManagerVehicleController(IVehiculoService vehiculoService,
            ISupabaseStorageService supabaseStorageService) {
        this.vehiculoService = vehiculoService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @PostMapping("/guardar-vehiculo")
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
            @RequestParam(value = "galeria1", required = false) MultipartFile galeria1,
            @RequestParam(value = "galeria2", required = false) MultipartFile galeria2,
            @RequestParam(value = "galeria3", required = false) MultipartFile galeria3,
            @RequestParam MultipartFile imagen,
            RedirectAttributes redirectAttributes) throws IOException {

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
                vehiculo.setUrlModelo3d(supabaseStorageService.uploadFile(modelo3d));
            } catch (Exception e) {
                System.err.println("Error 3D: " + e.getMessage());
            }
        }

        if (colores != null && !colores.isEmpty()) {
            vehiculo.setColores(Arrays.stream(colores.split(",")).map(String::trim).collect(Collectors.toList()));
        }

        List<MultipartFile> galeria = new ArrayList<>();
        if (galeria1 != null && !galeria1.isEmpty())
            galeria.add(galeria1);
        if (galeria2 != null && !galeria2.isEmpty())
            galeria.add(galeria2);
        if (galeria3 != null && !galeria3.isEmpty())
            galeria.add(galeria3);

        vehiculoService.crearVehiculoNormal(vehiculo, imagen, galeria);
        redirectAttributes.addFlashAttribute("success", "Vehículo guardado");
        return "redirect:/perfil_gestor";
    }

    @GetMapping("/obtener-vehiculo/{id}")
    @ResponseBody
    public Vehiculo obtenerVehiculoParaEdicion(@PathVariable String id) {
        return vehiculoService.obtenerPorId(id);
    }

    @PostMapping("/editar-vehiculo/{id}")
    public String editarVehiculo(
            @PathVariable String id,
            @ModelAttribute Vehiculo vehiculo,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam Integer pasajeros,
            @RequestParam String colores,
            @RequestParam String descripcion,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam(value = "galeria1", required = false) MultipartFile galeria1,
            @RequestParam(value = "galeria2", required = false) MultipartFile galeria2,
            @RequestParam(value = "galeria3", required = false) MultipartFile galeria3,
            RedirectAttributes redirectAttributes) {

        try {
            Vehiculo ve = vehiculoService.obtenerPorId(id);
            if (imagen != null && !imagen.isEmpty())
                vehiculoService.actualizarImagenVehiculo(ve, imagen);

            ve.setMarca(vehiculo.getMarca());
            ve.setModelo(vehiculo.getModelo());
            ve.setAño(vehiculo.getAño());
            ve.setPrecio(vehiculo.getPrecio());
            ve.setCategoria(vehiculo.getCategoria());
            ve.setMotor(motor);
            ve.setTransmision(transmision);
            ve.setCombustible(combustible);
            ve.setPasajeros(pasajeros);
            ve.setDescripcion(descripcion);

            if (modelo3d != null && !modelo3d.isEmpty()) {
                ve.setUrlModelo3d(supabaseStorageService.uploadFile(modelo3d));
            }

            if (colores != null && !colores.isEmpty()) {
                ve.setColores(Arrays.stream(colores.split(",")).map(String::trim).filter(c -> !c.isEmpty())
                        .collect(Collectors.toList()));
            }

            List<MultipartFile> nuevas = new ArrayList<>();
            if (galeria1 != null && !galeria1.isEmpty())
                nuevas.add(galeria1);
            if (galeria2 != null && !galeria2.isEmpty())
                nuevas.add(galeria2);
            if (galeria3 != null && !galeria3.isEmpty())
                nuevas.add(galeria3);
            if (!nuevas.isEmpty())
                vehiculoService.agregarImagenesGaleria(ve, nuevas);

            vehiculoService.guardarVehiculo(ve);
            redirectAttributes.addFlashAttribute("success", "Actualizado");
            return "redirect:/perfil_gestor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/perfil_gestor";
        }
    }

    @GetMapping("/eliminar-vehiculo/{id}")
    public String eliminarVehiculo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehiculoService.eliminarVehiculo(id);
        redirectAttributes.addFlashAttribute("success", "Eliminado");
        return "redirect:/perfil_gestor";
    }

    @PostMapping("/guardar-anuncio")
    public String guardarAnuncio(@ModelAttribute Vehiculo anuncio,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam MultipartFile imagen,
            @RequestParam String colores,
            RedirectAttributes redirectAttributes) {
        try {
            anuncio.setDestacado(true);
            if (modelo3d != null && !modelo3d.isEmpty())
                anuncio.setUrlModelo3d(supabaseStorageService.uploadFile(modelo3d));
            if (colores != null && !colores.isEmpty()) {
                anuncio.setColores(Arrays.stream(colores.split(",")).map(String::trim).collect(Collectors.toList()));
            }
            vehiculoService.crearAnuncio(anuncio, imagen);
            redirectAttributes.addFlashAttribute("success", "Anuncio creado");
            return "redirect:/perfil_gestor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/perfil_gestor";
        }
    }

    @GetMapping("/eliminar-anuncio/{id}")
    public String eliminarAnuncio(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehiculoService.eliminarVehiculo(id);
        redirectAttributes.addFlashAttribute("success", "Anuncio eliminado");
        return "redirect:/perfil_gestor";
    }
}
