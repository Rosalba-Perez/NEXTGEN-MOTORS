package com.concesionario.service.interfaces;

import com.concesionario.dto.ProspectoDTO;
import java.util.List;

public interface IProspectoService {
    List<ProspectoDTO> obtenerTodos();

    List<ProspectoDTO> obtenerProspectosParaAsesor(String asesorId);

    void registrarProspectoManual(String nombre, String apellido, String correo, String telefono,
            String vehiculoInteres, String asesorId, String observaciones);

    void cambiarEstadoContactado(String prospectoId);

    void actualizarEstadoProspecto(String prospectoId, String nuevoEstado);
}
