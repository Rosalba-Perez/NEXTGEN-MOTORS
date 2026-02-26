package com.concesionario.service;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Prospecto;
import com.concesionario.model.Trabajador;
import com.concesionario.repository.ProspectoRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.service.interfaces.IProspectoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProspectoServiceImpl implements IProspectoService {

    private final ProspectoRepository prospectoRepository;
    private final TrabajadorRepository trabajadorRepository;

    public ProspectoServiceImpl(ProspectoRepository prospectoRepository,
            TrabajadorRepository trabajadorRepository) {
        this.prospectoRepository = prospectoRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    @Override
    public List<ProspectoDTO> obtenerProspectosParaAsesor(String asesorId) {
        return prospectoRepository.findByAsesorId(asesorId).stream()
                .map(this::convertirAProspectoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void registrarProspectoManual(String nombre, String apellido, String correo, String telefono,
            String vehiculoInteres, String asesorId, String observaciones) {
        Prospecto prospecto = new Prospecto();
        prospecto.setNombre(nombre);
        prospecto.setApellido(apellido);
        prospecto.setCorreo(correo);
        prospecto.setTelefono(telefono);
        prospecto.setVehiculoInteres(vehiculoInteres);
        prospecto.setAsesorId(asesorId);
        prospecto.setObservaciones(observaciones);
        prospecto.setEstado("Pendiente");
        prospecto.setFechaRegistro(LocalDateTime.now());
        prospectoRepository.save(prospecto);
    }

    @Override
    public void cambiarEstadoContactado(String prospectoId) {
        actualizarEstadoProspecto(prospectoId, "Contactado");
    }

    @Override
    public void actualizarEstadoProspecto(String prospectoId, String nuevoEstado) {
        prospectoRepository.findById(prospectoId).ifPresent(p -> {
            p.setEstado(nuevoEstado);
            prospectoRepository.save(p);
        });
    }

    private ProspectoDTO convertirAProspectoDTO(Prospecto prospecto) {
        ProspectoDTO dto = new ProspectoDTO();
        dto.setId(prospecto.getId());
        dto.setNombre(prospecto.getNombre());
        dto.setApellido(prospecto.getApellido());
        dto.setCorreo(prospecto.getCorreo());
        dto.setTelefono(prospecto.getTelefono());
        dto.setVehiculoInteres(prospecto.getVehiculoInteres());
        dto.setEstado(prospecto.getEstado());
        dto.setFechaRegistro(prospecto.getFechaRegistro());
        return dto;
    }
}
