package com.concesionario.repository;

import com.concesionario.model.Junta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JuntaRepository extends MongoRepository<Junta, String> {
}
