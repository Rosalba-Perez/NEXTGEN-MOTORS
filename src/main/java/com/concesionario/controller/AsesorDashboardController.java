package com.concesionario.controller;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Trabajador;
import com.concesionario.model.Vehiculo;
import com.concesionario.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/asesor")
public class AsesorDashboardController {

    private final ITrabajadorDetailsService trabajadorDetailsService;
    private final IProspectoService prospectoService;
    private final IVehiculoService vehiculoService;
    private final IEmailService emailService;

    @Autowired
    public AsesorDashboardController(ITrabajadorDetailsService trabajadorDetailsService,
            IProspectoService prospectoService,
            IVehiculoService vehiculoService,
            IEmailService emailService) {
        this.trabajadorDetailsService = trabajadorDetailsService;
        this.prospectoService = prospectoService;
        this.vehiculoService = vehiculoService;
        this.emailService = emailService;
    }

    @GetMapping("/datos")
    public ResponseEntity<?> obtenerDatosAsesor(Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            List<ProspectoDTO> prospectos = prospectoService.obtenerProspectosParaAsesor(asesor.getId());
            LocalDateTime ahora = LocalDateTime.now();

            long prospectosActivos = prospectos.stream()
                    .filter(p -> !"Nuevo".equalsIgnoreCase(p.getEstado()))
                    .count();

            long ventasMes = prospectos.stream()
                    .filter(p -> "Venta Exitosa".equalsIgnoreCase(p.getEstado()) &&
                            p.getUltimoContacto() != null &&
                            p.getUltimoContacto().getMonth() == ahora.getMonth() &&
                            p.getUltimoContacto().getYear() == ahora.getYear())
                    .count();

            double tasaConversion = 0.0;
            if (prospectosActivos > 0) {
                tasaConversion = ((double) ventasMes / prospectosActivos) * 100.0;
            }

            double comisiones = ventasMes * 10000.0;

            Map<String, Object> response = new HashMap<>();
            response.put("nombre", asesor.getNombre());
            response.put("totalProspectos", prospectosActivos);
            response.put("ventasMes", ventasMes);
            response.put("tasaConversion", Math.round(tasaConversion * 10.0) / 10.0);
            response.put("comisiones", comisiones);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener datos: " + e.getMessage());
        }
    }

    @GetMapping("/vehiculos")
    public List<Map<String, Object>> obtenerVehiculosParaAsesor() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();
            return vehiculos.stream().map(vehiculo -> {
                Map<String, Object> vehiculoMap = new HashMap<>();
                vehiculoMap.put("id", vehiculo.getId());
                vehiculoMap.put("marca", vehiculo.getMarca());
                vehiculoMap.put("modelo", vehiculo.getModelo());
                vehiculoMap.put("año", vehiculo.getAño());
                vehiculoMap.put("precio", vehiculo.getPrecio());
                vehiculoMap.put("categoria", vehiculo.getCategoria());
                vehiculoMap.put("imagenUrl", vehiculo.getImagenUrl());
                vehiculoMap.put("motor", vehiculo.getMotor());
                vehiculoMap.put("transmision", vehiculo.getTransmision());
                vehiculoMap.put("combustible", vehiculo.getCombustible());
                vehiculoMap.put("pasajeros", vehiculo.getPasajeros());
                vehiculoMap.put("descripcion", vehiculo.getDescripcion());
                vehiculoMap.put("colores", vehiculo.getColores() != null ? vehiculo.getColores() : new ArrayList<>());
                vehiculoMap.put("destacado", vehiculo.isDestacado());
                return vehiculoMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener vehículos: " + e.getMessage());
        }
    }

    @GetMapping("/vehiculos/marcas")
    public List<String> obtenerMarcasDisponibles() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();
            return vehiculos.stream()
                    .map(Vehiculo::getMarca)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener marcas: " + e.getMessage());
        }
    }

    @GetMapping("/vehiculos/categorias")
    public List<String> obtenerCategoriasDisponibles() {
        try {
            List<Vehiculo> vehiculos = vehiculoService.obtenerTodos();
            return vehiculos.stream()
                    .map(Vehiculo::getCategoria)
                    .filter(categoria -> categoria != null && !categoria.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener categorías: " + e.getMessage());
        }
    }

    @PostMapping("/vehiculos/{id}/compartir")
    public ResponseEntity<?> compartirVehiculoConClientes(@PathVariable String id, Principal principal) {
        try {
            Trabajador asesor = trabajadorDetailsService.findByCorreo(principal.getName());
            emailService.enviarPromocionVehiculo(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Promoción enviada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}
