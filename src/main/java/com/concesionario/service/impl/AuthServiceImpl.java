package com.concesionario.service.impl;

import com.concesionario.model.Usuario;
import com.concesionario.model.Administrador;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.repository.AdministradorRepository;
import com.concesionario.service.interfaces.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Primero buscar en la nueva colección de Administradores
        Optional<Administrador> administrador = administradorRepository.findByCorreoAdmin(username);
        if (administrador.isPresent()) {
            Administrador admin = administrador.get();
            return User.builder()
                    .username(admin.getCorreoAdmin())
                    .password(admin.getPasswordAdmin())
                    .roles(admin.getRol().name())
                    .build();
        }

        // 2. Si no es administrador, buscar en la colección de Usuarios
        Optional<Usuario> usuario = usuarioRepository.findByCorreoUser(username);
        if (usuario.isPresent()) {
            Usuario user = usuario.get();
            return User.builder()
                    .username(user.getCorreoUser())
                    .password(user.getPasswordUser())
                    .roles(user.getRol().name())
                    .build();
        }

        // 3. Si no se encuentra en ninguna colección, lanzar excepción
        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}