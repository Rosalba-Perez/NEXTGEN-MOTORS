package com.concesionario.service.interfaces;

import java.util.Optional;

public interface IValidacionService {
    boolean existeCorreoEnCualquierUsuario(String correo);

    boolean existeIdentificacionEnCualquierUsuario(String identificacion);

    Optional<String> validarCorreoEIdentificacion(String correo, String identificacion);
}
