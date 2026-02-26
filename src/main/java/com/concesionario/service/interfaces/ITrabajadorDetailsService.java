package com.concesionario.service.interfaces;

import com.concesionario.model.Trabajador;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.List;
import java.util.Map;

public interface ITrabajadorDetailsService extends UserDetailsService {
    Trabajador findByCorreo(String correo);

    Map<String, Object> obtenerDatosAsesor(String asesorId);

    List<Map<String, Object>> obtenerProspectosParaAsesor(String asesorId);

    void marcarComoContactado(String citaId);

    List<Map<String, Object>> obtenerCitasProximas(String asesorId);
}
