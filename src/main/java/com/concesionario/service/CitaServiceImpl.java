package com.concesionario.service;

import com.concesionario.dto.UsuarioDTO;
import com.concesionario.dto.VehiculoDTO;
import com.concesionario.model.Cita;
import com.concesionario.model.Usuario;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.CitaRepository;
import com.concesionario.service.interfaces.ICitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements ICitaService {

    private final CitaRepository citaRepository;

    @Autowired
    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita guardarCita(Cita cita) {
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Cita> obtenerCitasPorTipo(String tipo) {
        return citaRepository.findByTipo(tipo);
    }

    @Override
    public List<Cita> obtenerCitasPendientes() {
        return citaRepository.findByAtendidaFalseOrderByIdDesc();
    }

    @Override
    public List<Cita> obtenerCitasPorUsuarioId(String usuarioId) {
        return citaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @Override
    public Cita obtenerCitaPorId(String id) {
        return citaRepository.findById(id).orElse(null);
    }

    @Override
    public void guardarCitaSimple(Cita cita, Usuario usuario, Vehiculo vehiculo) {
        prepararCitaEmbedding(cita, usuario, vehiculo);
        citaRepository.save(cita);
    }

    @Override
    public void crearCitaConEmbedding(Cita cita, Usuario usuario, Vehiculo vehiculo) {
        prepararCitaEmbedding(cita, usuario, vehiculo);
        citaRepository.save(cita);
    }

    private void prepararCitaEmbedding(Cita cita, Usuario usuario, Vehiculo vehiculo) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombreUser());
        usuarioDTO.setApellido(usuario.getApellidoUser());
        usuarioDTO.setCorreo(usuario.getCorreoUser());
        usuarioDTO.setIdentificacion(usuario.getIdentificacionUser());

        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        if (vehiculo != null) {
            vehiculoDTO.setId(vehiculo.getId());
            vehiculoDTO.setMarca(vehiculo.getMarca());
            vehiculoDTO.setModelo(vehiculo.getModelo());
            vehiculoDTO.setPrecio(vehiculo.getPrecio());
            vehiculoDTO.setCategoria(vehiculo.getCategoria());
        }

        cita.setUsuario(usuarioDTO);
        cita.setVehiculo(vehiculoDTO);
        cita.setFechaCreacion(LocalDateTime.now());
        cita.setAtendida(false);
        cita.setLeida(false);
        cita.setEstado("Pendiente");
    }

    @Override
    public long contarTodasLasCitas() {
        return citaRepository.count();
    }

    @Override
    public boolean isHoraDisponible(String trabajadorId, String fechaCita, String horaCita) {
        return citaRepository.findByTrabajadorIdAndFechaCitaAndHoraCita(trabajadorId, fechaCita, horaCita).isEmpty();
    }

    @Override
    public List<String> getHorasOcupadas(String trabajadorId, String fechaCita) {
        return citaRepository.findByTrabajadorIdAndFechaCita(trabajadorId, fechaCita).stream()
                .map(Cita::getHoraCita)
                .collect(Collectors.toList());
    }
}
