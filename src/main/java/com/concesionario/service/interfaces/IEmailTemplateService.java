package com.concesionario.service.interfaces;

import java.util.Map;

public interface IEmailTemplateService {
    String buildWelcomeEmail(String nombre);

    String crearContenidoBienvenidaHtml(String nombre, String apellido);

    String buildAppointmentConfirmationEmail(String nombre, String fecha, String hora);

    String buildPasswordResetEmail(String link);

    String crearContenidoPromocionalHtml(com.concesionario.model.Usuario usuario,
            com.concesionario.model.Vehiculo vehiculo);
}
