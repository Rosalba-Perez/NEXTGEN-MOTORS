package com.concesionario.service.interfaces;

import com.concesionario.model.Rol;
import com.concesionario.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    void registrarUsuario(String nombre, String apellido, String email, String identificacion, String password,
            Rol rol);

    long contarUsuarios();

    boolean existeCorreoEnCualquierTabla(String correo);

    boolean existeIdentificacionEnCualquierTabla(String identificacion);

    Usuario findByCorreoUser(String correo);

    void calcularDatosDesdeCitas(Usuario usuario);

    List<Usuario> findAll();

    Usuario findById(String id);

    Usuario save(Usuario usuario);

    Optional<Usuario> findByResetPasswordToken(String token);
}
