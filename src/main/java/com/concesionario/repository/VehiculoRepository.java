package com.concesionario.repository;

import com.concesionario.model.Vehiculo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface VehiculoRepository extends MongoRepository<Vehiculo, String> {
    List<Vehiculo> findByCategoria(String categoria);
    List<Vehiculo> findByDestacadoTrue();
    List<Vehiculo> findByDestacadoFalse();
    long countByDestacadoFalse();
    long countByDestacadoTrue();

    @org.springframework.data.mongodb.repository.Query("{ $or: [ { 'marca': { $regex: ?0, $options: 'i' } }, { 'modelo': { $regex: ?0, $options: 'i' } }, { 'categoria': { $regex: ?0, $options: 'i' } }, { 'descripcion': { $regex: ?0, $options: 'i' } } ] }")
    List<Vehiculo> findByFiltroRegex(String regex);
}