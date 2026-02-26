package com.concesionario.service.interfaces;

import com.concesionario.model.Vehiculo;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface IVehiculoService {
    List<Vehiculo> obtenerTodos();

    List<Vehiculo> obtenerDestacados();

    List<Vehiculo> obtenerVehiculosNormales();

    void crearVehiculoNormal(Vehiculo vehiculo, MultipartFile imagen, List<MultipartFile> otrasImagenes)
            throws IOException;

    void crearAnuncio(Vehiculo vehiculo, MultipartFile imagen) throws IOException;

    void eliminarVehiculo(String id);

    Vehiculo obtenerPorId(String id);

    void actualizarImagenVehiculo(Vehiculo vehiculo, MultipartFile imagen) throws IOException;

    void guardarVehiculo(Vehiculo vehiculo);

    long contarTodosVehiculos();

    List<Vehiculo> buscarVehiculos(String marca, String categoria, Integer añoMin, Integer añoMax, Double precioMin,
            Double precioMax, String transmision);

    List<Vehiculo> obtenerDestacadosLimit(int limit);
}
