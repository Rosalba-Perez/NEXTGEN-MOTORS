package com.concesionario.service.impl;

import com.concesionario.model.Rol;
import com.concesionario.model.Usuario;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.service.interfaces.IEmailService;
import com.concesionario.service.interfaces.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final IEmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final com.concesionario.repository.CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            IEmailService emailService,
            com.concesionario.repository.CitaRepository citaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.citaRepository = citaRepository;
    }

    @Override
    public void registrarUsuario(String nombre, String apellido,
            String email, String identificacion,
            String password, Rol rol) {

        String passwordEncriptado = passwordEncoder.encode(password);

        Usuario usuario = new Usuario();
        usuario.setNombreUser(nombre);
        usuario.setApellidoUser(apellido);
        usuario.setCorreoUser(email);
        usuario.setIdentificacionUser(identificacion);
        usuario.setPasswordUser(passwordEncriptado);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setRol(rol);

        emailService.enviarCorreoBienvenida(email, nombre, apellido);

        usuarioRepository.save(usuario);
    }

    @Override
    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    @Override
    public boolean existeCorreoEnCualquierTabla(String correo) {
        return usuarioRepository.existsByCorreoUser(correo);
    }

    @Override
    public boolean existeIdentificacionEnCualquierTabla(String identificacion) {
        return usuarioRepository.existsByIdentificacionUser(identificacion);
    }

    @Override
    public Usuario findByCorreoUser(String correo) {
        return usuarioRepository.findByCorreoUser(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario findById(String id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findByResetPasswordToken(String token) {
        return usuarioRepository.findByResetPasswordToken(token);
    }

    @Override
    public void calcularDatosDesdeCitas(Usuario usuario) {
        var citas = citaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());

        LocalDateTime fechaCreacionUsuario = usuario.getFechaCreacion();
        if (fechaCreacionUsuario == null) {
            fechaCreacionUsuario = LocalDateTime.now();
        }

        long antiguedad = java.time.temporal.ChronoUnit.DAYS.between(fechaCreacionUsuario, LocalDateTime.now());
        usuario.setAntiguedadCuenta((int) Math.max(0, antiguedad));

        if (citas.isEmpty()) {
            usuario.setCantidadCitas(0);
            usuario.setEstadoUltimaCita("Pendiente");
            usuario.setInteresVehiculo("No");
            usuario.setTiempoEntreCitas(0);
            return;
        }

        usuario.setCantidadCitas(citas.size());
        var ultimaCita = citas.get(0);
        usuario.setEstadoUltimaCita(ultimaCita.getEstado());

        String interes = "No";
        if (citas.size() >= 2 || "Aprobada".equals(ultimaCita.getEstado())) {
            interes = "Si";
        }
        usuario.setInteresVehiculo(interes);

        if (citas.size() > 1) {
            long totalDias = 0;
            for (int i = citas.size() - 1; i > 0; i--) {
                long dias = java.time.temporal.ChronoUnit.DAYS.between(citas.get(i).getFechaCreacion(),
                        citas.get(i - 1).getFechaCreacion());
                totalDias += Math.abs(dias);
            }
            usuario.setTiempoEntreCitas((int) (totalDias / (citas.size() - 1)));
        } else {
            usuario.setTiempoEntreCitas(30);
        }
    }
}
