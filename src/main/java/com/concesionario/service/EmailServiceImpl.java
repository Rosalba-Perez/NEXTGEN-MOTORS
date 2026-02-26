package com.concesionario.service;

import com.concesionario.model.Usuario;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.repository.VehiculoRepository;
import com.concesionario.service.interfaces.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class EmailServiceImpl implements IEmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EmailTemplateService templateService;

    public EmailServiceImpl(JavaMailSender mailSender,
            UsuarioRepository usuarioRepository,
            VehiculoRepository vehiculoRepository,
            EmailTemplateService templateService) {
        this.mailSender = mailSender;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.templateService = templateService;
    }

    @Override
    public void enviarPromocionVehiculo(String vehiculoId) {
        try {
            Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                    .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

            List<Usuario> usuarios = usuarioRepository.findAll();

            if (usuarios.isEmpty()) {
                throw new RuntimeException("No hay usuarios registrados");
            }

            int exitosos = 0;
            int fallidos = 0;

            for (Usuario usuario : usuarios) {
                try {
                    enviarCorreoPromocionalHtmlConImagen(usuario, vehiculo);
                    exitosos++;
                    Thread.sleep(50);
                } catch (Exception e) {
                    System.err.println(
                            "❌ Error enviando promoción a: " + usuario.getCorreoUser() + " - " + e.getMessage());
                    fallidos++;
                }
            }

            System.out.println("✅ Envío masivo completado: " + exitosos + " exitosos, " + fallidos + " fallidos");

        } catch (Exception e) {
            throw new RuntimeException("Error en envío masivo: " + e.getMessage());
        }
    }

    private void enviarCorreoPromocionalHtmlConImagen(Usuario usuario, Vehiculo vehiculo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        setFromEmail(helper);
        helper.setTo(usuario.getCorreoUser());
        helper.setSubject("🚗 ¡Nueva Oportunidad! " + vehiculo.getMarca() + " " + vehiculo.getModelo());

        String contenidoHtml = templateService.crearContenidoPromocionalHtml(usuario, vehiculo);
        helper.setText(contenidoHtml, true);

        mailSender.send(message);
    }

    @Override
    public void enviarCorreoBienvenida(String email, String nombre, String apellido) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            setFromEmail(helper);
            helper.setTo(email);
            helper.setSubject("¡Bienvenido a NextGen Motors, " + nombre + "! 🚗");

            String contenidoHtml = templateService.crearContenidoBienvenidaHtml(nombre, apellido);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            System.out.println("✅ Correo de bienvenida enviado a: " + email);

        } catch (MessagingException e) {
            System.err.println("❌ Error enviando correo de bienvenida: " + e.getMessage());
            throw new RuntimeException("Error enviando correo de bienvenida", e);
        }
    }

    private void setFromEmail(MimeMessageHelper helper) throws MessagingException {
        try {
            helper.setFrom(fromEmail, "NextGen Motors");
        } catch (UnsupportedEncodingException e) {
            helper.setFrom(fromEmail);
        }
    }
}
