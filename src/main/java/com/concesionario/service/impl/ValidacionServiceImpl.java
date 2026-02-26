package com.concesionario.service.impl;

import com.concesionario.repository.AdministradorRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.service.interfaces.IValidacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ValidacionServiceImpl implements IValidacionService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TrabajadorRepository trabajadorRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    public boolean existeCorreoEnCualquierUsuario(String correo) {
        return usuarioRepository.existsByCorreoUser(correo) ||
                trabajadorRepository.findByCorreo(correo).isPresent() ||
                administradorRepository.existsByCorreoAdmin(correo);
    }

    public boolean existeIdentificacionEnCualquierUsuario(String identificacion) {
        return usuarioRepository.existsByIdentificacionUser(identificacion) ||
                trabajadorRepository.findByIdentificacion(identificacion).isPresent() ||
                administradorRepository.existsByIdentificacionAdmin(identificacion);
    }

    public Optional<String> validarCorreoEIdentificacion(String correo, String identificacion) {
        if (existeCorreoEnCualquierUsuario(correo)) {
            return Optional.of("El correo ya está registrado en el sistema");
        }
        if (existeIdentificacionEnCualquierUsuario(identificacion)) {
            return Optional.of("La identificación ya está registrada en el sistema");
        }
        return Optional.empty();
    }
}