package com.concesionario.service.interfaces;

import com.concesionario.model.Sede;
import java.util.List;

public interface ISedeService {
    List<Sede> listarTodas();

    Sede buscarPorId(String id);

    void inicializarSedes();
}
