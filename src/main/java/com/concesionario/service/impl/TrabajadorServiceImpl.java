package com.concesionario.service.impl;

import com.concesionario.model.Trabajador;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.service.interfaces.ITrabajadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class TrabajadorServiceImpl implements ITrabajadorService {
    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;

    public TrabajadorServiceImpl(TrabajadorRepository trabajadorRepository,
            PasswordEncoder passwordEncoder) {
        this.trabajadorRepository = trabajadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existeCorreo(String correo) {
        return trabajadorRepository.findByCorreo(correo).isPresent();
    }

    public boolean existeIdentificacion(String identificacion) {
        return trabajadorRepository.findByIdentificacion(identificacion).isPresent();
    }

    public void registrarTrabajador(String nombre, String apellido, String correo,
            String identificacion, String password,
            LocalTime horaInicio, LocalTime horaFin,
            List<String> diasTrabajo, String sedeId) {
        Trabajador trabajador = new Trabajador();
        trabajador.setNombre(nombre);
        trabajador.setApellido(apellido);
        trabajador.setCorreo(correo);
        trabajador.setIdentificacion(identificacion);
        trabajador.setPassword(passwordEncoder.encode(password));
        trabajador.setHoraInicioTrabajo(horaInicio);
        trabajador.setHoraFinTrabajo(horaFin);
        trabajador.setDiasTrabajo(diasTrabajo);
        trabajador.setSedeId(sedeId);
        trabajador.setActivo(true); // Ensure new workers are active

        trabajadorRepository.save(trabajador);
    }

    public List<Trabajador> listarTodos() {
        return trabajadorRepository.findAll();
    }

    public void desactivarTrabajador(String id) {
        trabajadorRepository.findById(id).ifPresent(trabajador -> {
            trabajador.setActivo(false);
            trabajadorRepository.save(trabajador);
        });
    }

    @Override
    public List<Trabajador> listarActivos() {
        return trabajadorRepository.findByActivo(true);
    }

    @Override
    public long contarTrabajadores() {
        return trabajadorRepository.count();
    }

    @Override
    public Trabajador findByCorreo(String correo) {
        return trabajadorRepository.findByCorreo(correo).orElse(null);
    }

    @Override
    public Trabajador obtenerPorId(String id) {
        return trabajadorRepository.findById(id).orElse(null);
    }

    @Override
    public List<Trabajador> listarPorRol(com.concesionario.model.Rol rol) {
        return trabajadorRepository.findByRolesContaining(rol);
    }

    @Override
    public void setRoles(String correo, List<com.concesionario.model.Rol> roles) {
        trabajadorRepository.findByCorreo(correo).ifPresent(t -> {
            t.setRoles(roles);
            trabajadorRepository.save(t);
        });
    }
}