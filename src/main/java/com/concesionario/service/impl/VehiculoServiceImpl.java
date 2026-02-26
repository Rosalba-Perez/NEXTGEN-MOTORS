package com.concesionario.service.impl;

import com.concesionario.model.Vehiculo;
import com.concesionario.repository.VehiculoRepository;
import com.concesionario.service.interfaces.IVehiculoService;
import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VehiculoServiceImpl implements IVehiculoService {
    private static final Logger log = LoggerFactory.getLogger(VehiculoServiceImpl.class);

    private final VehiculoRepository vehiculoRepository;
    private final Cloudinary cloudinary;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository, Cloudinary cloudinary) {
        this.vehiculoRepository = vehiculoRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public List<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Vehiculo> obtenerVehiculosNormales() {
        return vehiculoRepository.findByDestacadoFalse();
    }

    @Override
    public void crearVehiculoNormal(Vehiculo vehiculo, MultipartFile imagen, List<MultipartFile> galeriaImagenes)
            throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);

        if (galeriaImagenes != null && !galeriaImagenes.isEmpty()) {
            List<String> urlsGaleria = new ArrayList<>();
            for (MultipartFile img : galeriaImagenes) {
                if (img != null && !img.isEmpty()) {
                    urlsGaleria.add(guardarImagenEnCloudinary(img));
                }
            }
            vehiculo.setGaleria(urlsGaleria);
        }

        vehiculo.setDestacado(false);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    public List<Vehiculo> obtenerDestacados() {
        try {
            List<Vehiculo> destacados = vehiculoRepository.findByDestacadoTrue();
            return destacados != null ? destacados : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error al obtener vehiculos destacados", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void crearAnuncio(Vehiculo vehiculo, MultipartFile imagen) throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);
        vehiculo.setDestacado(true);
        vehiculoRepository.save(vehiculo);
    }

    @Override
    public void crearAnuncioCompleto(Vehiculo vehiculo, MultipartFile imagen) throws IOException {
        crearAnuncio(vehiculo, imagen);
    }

    @Override
    public Vehiculo obtenerPorId(String id) {
        return vehiculoRepository.findById(id).orElse(null);
    }

    @Override
    public void guardarVehiculo(Vehiculo vehiculo) {
        vehiculoRepository.save(vehiculo);
    }

    @Override
    public void eliminarVehiculo(String id) {
        vehiculoRepository.deleteById(id);
    }

    @Override
    public List<Vehiculo> obtenerPorCategoria(String categoria) {
        return vehiculoRepository.findByCategoria(categoria);
    }

    @Override
    public long contarTodosVehiculos() {
        return vehiculoRepository.count();
    }

    @Override
    public long contarVehiculosNormales() {
        return vehiculoRepository.countByDestacadoFalse();
    }

    @Override
    public long contarAnuncios() {
        return vehiculoRepository.countByDestacadoTrue();
    }

    @Override
    public List<Vehiculo> buscarVehiculos(String marca, String modelo,
            Integer anioMin, Integer anioMax,
            Double precioMin, Double precioMax,
            String categoria) {
        List<Vehiculo> todos = obtenerTodos();
        return todos.stream()
                .filter(v -> {
                    if (marca != null && !marca.isEmpty() &&
                            (v.getMarca() == null || !v.getMarca().toLowerCase().contains(marca.toLowerCase()))) {
                        return false;
                    }
                    if (modelo != null && !modelo.isEmpty() &&
                            (v.getModelo() == null || !v.getModelo().toLowerCase().contains(modelo.toLowerCase()))) {
                        return false;
                    }
                    if (anioMin != null && v.getAño() < anioMin)
                        return false;
                    if (anioMax != null && v.getAño() > anioMax)
                        return false;
                    if (precioMin != null && v.getPrecio() < precioMin)
                        return false;
                    if (precioMax != null && v.getPrecio() > precioMax)
                        return false;
                    if (categoria != null && !categoria.isEmpty() &&
                            (v.getCategoria() == null || !v.getCategoria().equalsIgnoreCase(categoria))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private String guardarImagenEnCloudinary(MultipartFile imagen) throws IOException {
        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", "auto_plus/vehiculos");
            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(imagen.getBytes(), uploadOptions);
            return uploadResult.get("url").toString();
        } catch (Exception e) {
            log.error("Error al subir imagen a Cloudinary", e);
            throw new IOException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarImagenVehiculo(Vehiculo vehiculo, MultipartFile imagen) throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);
    }

    @Override
    public void agregarImagenesGaleria(Vehiculo vehiculo, List<MultipartFile> nuevasImagenes) throws IOException {
        if (nuevasImagenes == null || nuevasImagenes.isEmpty())
            return;
        List<String> galeriaActual = vehiculo.getGaleria();
        if (galeriaActual == null) {
            galeriaActual = new ArrayList<>();
        }
        for (MultipartFile img : nuevasImagenes) {
            if (img != null && !img.isEmpty()) {
                galeriaActual.add(guardarImagenEnCloudinary(img));
            }
        }
        vehiculo.setGaleria(galeriaActual);
    }

    @Override
    public List<Vehiculo> obtenerDestacadosLimit(int limit) {
        return vehiculoRepository.findByDestacadoTrue().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
