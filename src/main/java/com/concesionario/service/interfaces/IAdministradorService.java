package com.concesionario.service.interfaces;

import com.concesionario.model.Administrador;

public interface IAdministradorService {
    void registrarAdministrador(String nombre, String apellido, String identificacion, String email, String password);

    Administrador findByCorreoAdmin(String email);
}
