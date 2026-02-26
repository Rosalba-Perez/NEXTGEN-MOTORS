package com.concesionario.service.interfaces;

import com.concesionario.model.Cita;
import java.util.List;

public interface IAdminNotificationService {
    long contarCitasNoLeidas();

    List<Cita> obtenerCitasNoLeidas();

    void marcarComoLeida(String id);

    void marcarTodasComoLeidas();
}
