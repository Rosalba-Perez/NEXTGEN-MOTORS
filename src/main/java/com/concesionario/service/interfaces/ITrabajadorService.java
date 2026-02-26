package com.concesionario.service.interfaces;

import com.concesionario.model.Rol;
import com.concesionario.model.Trabajador;
import java.time.LocalTime;
import java.util.List;

public interface ITrabajadorService {
    boolean existeCorreo(String correo);

    boolean existeIdentificacion(String identificacion);

    void registrarTrabajador(String nombre, String apellido, String correo, String identificacion, String password,
            LocalTime horaInicio, LocalTime horaFin, List<String> diasTrabajo, String sedeId);

    List<Trabajador> listarTodos();

    void desactivarTrabajador(String id);

    List<Trabajador> listarActivos();

    long contarTrabajadores();

    Trabajador findByCorreo(String correo);

    Trabajador obtenerPorId(String id);

    List<Trabajador> listarPorRol(Rol rol);

    void setRoles(String correo, List<Rol> roles);
}
