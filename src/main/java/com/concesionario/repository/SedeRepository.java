package com.concesionario.repository;

import com.concesionario.model.Sede;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SedeRepository extends MongoRepository<Sede, String> {
    Optional<Sede> findByNombre(String nombre);
}
