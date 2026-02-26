package com.concesionario.service.interfaces;

public interface IPushNotificationService {
    void enviarNotificacion(String userId, String titulo, String mensaje);
}
