package com.concesionario.service.interfaces;

import com.concesionario.model.Cita;
import com.concesionario.model.Usuario;
import com.concesionario.model.Vehiculo;
import java.util.List;

public interface ICitaService {
    Cita guardarCita(Cita cita);

    List<Cita> obtenerTodasLasCitas();

    List<Cita> obtenerCitasPorTipo(String tipo);

    List<Cita> obtenerCitasPendientes();

    List<Cita> obtenerCitasPorUsuarioId(String usuarioId);

    Cita obtenerCitaPorId(String id);

    void guardarCitaSimple(Cita cita, Usuario usuario, Vehiculo vehiculo);

    void crearCitaConEmbedding(Cita cita, Usuario usuario, Vehiculo vehiculo);

    long contarTodasLasCitas();

    boolean isHoraDisponible(String trabajadorId, String fechaCita, String horaCita);

    List<String> getHorasOcupadas(String trabajadorId, String fechaCita);
}
