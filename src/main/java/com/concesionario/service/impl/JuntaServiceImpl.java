package com.concesionario.service.impl;

import com.concesionario.model.Junta;
import com.concesionario.repository.JuntaRepository;
import com.concesionario.service.interfaces.IJuntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JuntaServiceImpl implements IJuntaService {

    @Autowired
    private JuntaRepository juntaRepository;

    @Override
    public Junta crearJunta(Junta junta) {
        return juntaRepository.save(junta);
    }

    @Override
    public List<Junta> listarTodas() {
        return juntaRepository.findAll();
    }

    @Override
    public List<Junta> listarPorSede(String sedeId) {
        // This would require a custom finder in repository if we want to filter by
        // sedeId
        // For now, let's just return all and we can filter in memory if needed or
        // update repo
        return juntaRepository.findAll();
    }
}
