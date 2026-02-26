package com.concesionario.service.impl;

import com.concesionario.model.Cita;
import com.concesionario.model.Usuario;
import com.concesionario.repository.CitaRepository;
import com.concesionario.service.interfaces.IAdminNotificationService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminNotificationServiceImpl implements IAdminNotificationService {

    @Autowired
    private CitaRepository citaRepository;

    public long contarCitasNoLeidas() {
        return citaRepository.countByLeidaFalse();
    }

    public List<Cita> obtenerCitasNoLeidas() {
        return citaRepository.findByLeidaFalseOrderByFechaCreacionDesc();
    }

    public void marcarComoLeida(String id) {
        citaRepository.findById(id).ifPresent(cita -> {
            cita.setLeida(true);
            citaRepository.save(cita);
        });
    }

    public void marcarTodasComoLeidas() {
        List<Cita> citasNoLeidas = citaRepository.findByLeidaFalse();
        citasNoLeidas.forEach(cita -> cita.setLeida(true));
        citaRepository.saveAll(citasNoLeidas);
    }
}