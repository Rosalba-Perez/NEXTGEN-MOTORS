package com.concesionario.controller;

import com.concesionario.dto.RecomendacionResponse;
import com.concesionario.model.Cita;
import com.concesionario.model.Usuario;
import com.concesionario.service.interfaces.ICitaService;
import com.concesionario.service.interfaces.IUsuarioService;
import com.concesionario.service.interfaces.IVehiculoRecomendacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "*")
public class MobileUserController {

    private final IUsuarioService usuarioService;
    private final ICitaService citaService;
    private final PasswordEncoder passwordEncoder;
    private final IVehiculoRecomendacionService vehiculoRecomendacionService;

    @Autowired
    public MobileUserController(IUsuarioService usuarioService,
            ICitaService citaService,
            PasswordEncoder passwordEncoder,
            IVehiculoRecomendacionService vehiculoRecomendacionService) {
        this.usuarioService = usuarioService;
        this.citaService = citaService;
        this.passwordEncoder = passwordEncoder;
        this.vehiculoRecomendacionService = vehiculoRecomendacionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String correo, @RequestParam String password) {
        Usuario usuario = usuarioService.findByCorreoUser(correo);
        if (usuario != null && passwordEncoder.matches(password, usuario.getPasswordUser())) {
            return ResponseEntity.ok(Map.of("success", true, "userId", usuario.getId(), "nombre",
                    usuario.getNombreUser(), "message", "Login exitoso"));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Credenciales inválidas"));
    }

    @GetMapping("/{userId}/citas")
    public ResponseEntity<?> obtenerCitas(@PathVariable String userId) {
        List<Cita> citas = citaService.obtenerCitasPorUsuarioId(userId);
        return ResponseEntity.ok(citas.stream().map(cita -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cita.getId());
            map.put("fechaSolicitud", cita.getFechaCreacion());
            map.put("fechaAsignada", cita.getFechaAsignada());
            map.put("estado", cita.getEstado());
            map.put("comentario", cita.getComentario());
            map.put("notasAdmin", cita.getNotasAdmin());
            map.put("vehiculo",
                    cita.getVehiculo() != null ? cita.getVehiculo().getMarca() + " " + cita.getVehiculo().getModelo()
                            : cita.getNombreVehiculo());
            return map;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/{userId}/fcm-token")
    public ResponseEntity<?> actualizarFcmToken(@PathVariable String userId, @RequestParam String token) {
        Usuario usuario = usuarioService.findById(userId);
        if (usuario == null)
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Usuario no encontrado"));
        usuario.setFcmToken(token);
        usuarioService.save(usuario);
        return ResponseEntity.ok(Map.of("success", true, "message", "Token actualizado"));
    }

    @PostMapping("/recomendacion")
    public ResponseEntity<?> obtenerRecomendacion(@RequestBody Map<String, String> request) {
        String mensaje = request.get("mensaje");
        if (mensaje == null || mensaje.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("respuesta", "Escribe un mensaje."));
        return ResponseEntity.ok(vehiculoRecomendacionService.procesarRecomendacion(mensaje));
    }
}
