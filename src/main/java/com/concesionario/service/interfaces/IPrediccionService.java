package com.concesionario.service.interfaces;

import com.concesionario.model.Usuario;

public interface IPrediccionService {
    String predecir(double citas, double antiguedad, String estado, String interes, double tiempo);

    double obtenerProbabilidadSi(double citas, double antiguedad, String estado, String interes, double tiempo);

    Usuario aplicarPrediccionYActualizar(Usuario usuario);

    String generarObservaciones(String prediccion, double probabilidad, double citas, String estado);
}
