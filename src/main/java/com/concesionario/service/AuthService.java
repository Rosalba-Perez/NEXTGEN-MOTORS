package com.concesionario.service;

import com.concesionario.model.Usuario;
import com.concesionario.model.Administrador;
import com.concesionario.repository.UsuarioRepository;
import com.concesionario.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private TrabajadorDetailsService trabajadorDetailsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Primero buscar en administradores
        try {
            Optional<Administrador> administrador = administradorRepository.findByCorreoAdmin(username);
            if (administrador.isPresent()) {
                Administrador admin = administrador.get();
                return User.builder()
                        .username(admin.getCorreoAdmin())
                        .password(admin.getPasswordAdmin())
                        .roles(admin.getRol().name())
                        .build();
            }
        } catch (Exception e) {

        }

        // 2. Luego buscar en usuarios
        try {
            Optional<Usuario> usuario = usuarioRepository.findByCorreoUser(username);
            if (usuario.isPresent()) {
                Usuario user = usuario.get();
                return User.builder()
                        .username(user.getCorreoUser())
                        .password(user.getPasswordUser())
                        .roles(user.getRol().name())
                        .build();
            }
        } catch (Exception e) {

        }

        // 3. Finalmente buscar en trabajadores
        try {
            return trabajadorDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // Si tampoco encuentra en trabajadores, lanzar la excepción
            throw new UsernameNotFoundException("Usuario no encontrado en ningún repositorio: " + username);
        }
    }
}