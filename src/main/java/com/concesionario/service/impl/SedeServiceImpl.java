package com.concesionario.service.impl;

import com.concesionario.model.Sede;
import com.concesionario.repository.SedeRepository;
import com.concesionario.service.interfaces.ISedeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class SedeServiceImpl implements ISedeService {

    @Autowired
    private SedeRepository sedeRepository;

    @Override
    public List<Sede> listarTodas() {
        return sedeRepository.findAll();
    }

    @Override
    public Sede buscarPorId(String id) {
        return sedeRepository.findById(id).orElse(null);
    }

    @Override
    @PostConstruct
    public void inicializarSedes() {
        if (sedeRepository.count() == 0) {
            sedeRepository.save(new Sede("NexGen Motors Bocagrande", "Avenida San Martín #12-34"));
            sedeRepository.save(new Sede("NexGen Motors La Boquilla", "Calle 8 #10-15"));
            sedeRepository.save(new Sede("NexGen Motors El Laguito", "Avenida del Retorno #5-67"));
            sedeRepository.save(new Sede("NexGen Motors Manga", "Calle 30 #25-45"));
        }
    }
}
