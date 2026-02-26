package com.concesionario.dto;

import java.time.LocalDateTime;

public class ProspectoDTO {
    private String id; // ID del Prospecto (antes citaId)
    private String usuarioId; // Opcional, si está registrado
    private String trabajadorId; // ID del asesor asignado
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String vehiculoInteres;
    private String estado;
    private LocalDateTime fechaRegistro;
    private String origen;
    private String observaciones;
    private LocalDateTime ultimoContacto;

    public ProspectoDTO() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(String trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getVehiculoInteres() {
        return vehiculoInteres;
    }

    public void setVehiculoInteres(String vehiculoInteres) {
        this.vehiculoInteres = vehiculoInteres;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getUltimoContacto() {
        return ultimoContacto;
    }

    public void setUltimoContacto(LocalDateTime ultimoContacto) {
        this.ultimoContacto = ultimoContacto;
    }
}