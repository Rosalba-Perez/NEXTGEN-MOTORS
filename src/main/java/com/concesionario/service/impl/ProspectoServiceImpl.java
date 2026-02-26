package com.concesionario.service.impl;

import com.concesionario.dto.ProspectoDTO;
import com.concesionario.model.Prospecto;
import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import com.concesionario.repository.ProspectoRepository;
import com.concesionario.repository.TrabajadorRepository;
import com.concesionario.service.interfaces.IProspectoService;
import com.concesionario.service.interfaces.IExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProspectoServiceImpl implements IProspectoService {

    private final ProspectoRepository prospectoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final IExcelExportService excelExportService;

    public ProspectoServiceImpl(ProspectoRepository prospectoRepository,
            TrabajadorRepository trabajadorRepository,
            IExcelExportService excelExportService) {
        this.prospectoRepository = prospectoRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.excelExportService = excelExportService;
    }

    @Override
    public List<ProspectoDTO> obtenerTodos() {
        return prospectoRepository.findAll().stream()
                .map(this::convertirAProspectoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProspectoDTO> obtenerProspectosParaAsesor(String asesorId) {
        return prospectoRepository.findByTrabajadorId(asesorId).stream()
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
        prospecto.setTrabajadorId(asesorId);
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
        dto.setTrabajadorId(prospecto.getTrabajadorId());
        dto.setNombre(prospecto.getNombre());
        dto.setApellido(prospecto.getApellido());
        dto.setCorreo(prospecto.getCorreo());
        dto.setTelefono(prospecto.getTelefono());
        dto.setVehiculoInteres(prospecto.getVehiculoInteres());
        dto.setEstado(prospecto.getEstado());
        dto.setFechaRegistro(prospecto.getFechaRegistro());
        dto.setUltimoContacto(prospecto.getUltimoContacto());
        return dto;
    }

    @Override
    public ByteArrayInputStream generarReporteRendimientoMensual() throws IOException {
        List<Trabajador> asesores = trabajadorRepository.findByRolesContaining(Rol.TRB_ASESOR);
        List<Prospecto> todosLosProspectos = prospectoRepository.findAll();

        List<Map<String, Object>> data = new ArrayList<>();
        String[] headers = { "Asesor", "Total Prospectos", "Contactados", "En Proceso", "Ventas", "Perdidos",
                "% Conversión" };

        for (Trabajador asesor : asesores) {
            List<Prospecto> prospectosAsesor = todosLosProspectos.stream()
                    .filter(p -> asesor.getId().equals(p.getTrabajadorId()))
                    .collect(Collectors.toList());

            long total = prospectosAsesor.size();
            long contactados = prospectosAsesor.stream().filter(p -> "Contactado".equals(p.getEstado())).count();
            long enProceso = prospectosAsesor.stream().filter(p -> "En Proceso".equals(p.getEstado())).count();
            long ventas = prospectosAsesor.stream().filter(p -> "Venta".equals(p.getEstado())).count();
            long perdidos = prospectosAsesor.stream().filter(p -> "Perdido".equals(p.getEstado())).count();

            double conversion = total > 0 ? (double) ventas / total : 0;

            Map<String, Object> row = new HashMap<>();
            row.put("Asesor", asesor.getNombre() + " " + asesor.getApellido());
            row.put("Total Prospectos", total);
            row.put("Contactados", contactados);
            row.put("En Proceso", enProceso);
            row.put("Ventas", ventas);
            row.put("Perdidos", perdidos);
            row.put("% Conversión", String.format("%.2f%%", conversion * 100));
            data.add(row);
        }

        return excelExportService.exportToExcel("Rendimiento Asesores", headers, data);
    }
}
