package com.concesionario.service;

import com.concesionario.service.interfaces.IAuthService;
import com.concesionario.service.interfaces.ITrabajadorDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CombinedUserDetailsService implements UserDetailsService {

    @Autowired
    private IAuthService authService;

    @Autowired
    private ITrabajadorDetailsService trabajadorDetailsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Primero intenta con usuarios normales
            return authService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // Si no encuentra usuario, intenta con trabajador
            return trabajadorDetailsService.loadUserByUsername(username);
        }
    }
}