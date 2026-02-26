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

        void crearAnuncioCompleto(Vehiculo vehiculo, MultipartFile imagen) throws IOException;

        void eliminarVehiculo(String id);

        Vehiculo obtenerPorId(String id);

        void actualizarImagenVehiculo(Vehiculo vehiculo, MultipartFile imagen) throws IOException;

        void agregarImagenesGaleria(Vehiculo vehiculo, List<MultipartFile> nuevasImagenes) throws IOException;

        void guardarVehiculo(Vehiculo vehiculo);

        long contarTodosVehiculos();

        long contarVehiculosNormales();

        long contarAnuncios();

        List<Vehiculo> obtenerPorCategoria(String categoria);

        List<Vehiculo> buscarVehiculos(String marca, String modelo, Integer anioMin, Integer anioMax, Double precioMin,
                        Double precioMax, String categoria);

        List<Vehiculo> obtenerDestacadosLimit(int limit);
}
