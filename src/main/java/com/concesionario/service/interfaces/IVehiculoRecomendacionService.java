package com.concesionario.service.interfaces;

import com.concesionario.dto.RecomendacionResponse;

public interface IVehiculoRecomendacionService {
    RecomendacionResponse procesarRecomendacion(String mensajeUsuario);
}
