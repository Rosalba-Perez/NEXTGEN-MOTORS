package com.concesionario.service.interfaces;

public interface IEmailService {
    void enviarCorreoBienvenida(String destinatario, String nombre, String apellido);

    void enviarPromocionVehiculo(String vehiculoId);
}
