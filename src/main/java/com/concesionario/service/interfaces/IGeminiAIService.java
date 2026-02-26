package com.concesionario.service.interfaces;

import com.concesionario.model.Vehiculo;
import reactor.core.publisher.Mono;
import java.util.List;

public interface IGeminiAIService {
    Mono<String> analizarYSeleccionarVehiculo(String mensajeUsuario, List<Vehiculo> vehiculosDisponibles);
}
