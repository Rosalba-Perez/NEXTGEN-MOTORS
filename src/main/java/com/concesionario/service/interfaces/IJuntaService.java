package com.concesionario.service.interfaces;

import com.concesionario.model.Junta;
import java.util.List;

public interface IJuntaService {
    Junta crearJunta(Junta junta);

    List<Junta> listarTodas();

    List<Junta> listarPorSede(String sedeId);
}
