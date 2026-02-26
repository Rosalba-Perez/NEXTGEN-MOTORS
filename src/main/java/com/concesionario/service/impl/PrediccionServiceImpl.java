package com.concesionario.service.impl;

import com.concesionario.model.Usuario;
import com.concesionario.service.interfaces.IPrediccionService;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.InputStream;
import java.util.ArrayList;

@Service
public class PrediccionServiceImpl implements IPrediccionService {

    private Classifier modelo;
    private Instances estructura;

    public PrediccionServiceImpl() {
        try {
            System.out.println(" Cargando modelo y estructura...");

            // Cargar modelo
            InputStream modeloStream = getClass().getClassLoader().getResourceAsStream("modelo_PA.model");
            if (modeloStream == null) {
                throw new RuntimeException(" No se encontró modelo_PA.model");
            }
            modelo = (Classifier) weka.core.SerializationHelper.read(modeloStream);

            // Cargar estructura desde el ARFF
            InputStream estructuraStream = getClass().getClassLoader().getResourceAsStream("estructura.arff");
            if (estructuraStream == null) {
                throw new RuntimeException(" No se encontró estructura.arff");
            }

            DataSource source = new DataSource(estructuraStream);
            estructura = source.getDataSet();
            estructura.setClassIndex(estructura.numAttributes() - 1);

            System.out.println("✅ Modelo cargado: " + modelo.getClass().getSimpleName());
            System.out.println("✅ Estructura cargada: " + estructura.numAttributes() + " atributos");
            System.out.println("✅ Clase: " + estructura.classAttribute().name());

        } catch (Exception e) {
            System.err.println(" Error cargando modelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String predecir(double citas, double antiguedad, String estado, String interes, double tiempo) {
        try {
            Instance nuevaInstancia = new weka.core.DenseInstance(estructura.numAttributes());
            nuevaInstancia.setDataset(estructura);

            nuevaInstancia.setValue(0, citas);
            nuevaInstancia.setValue(1, antiguedad);
            nuevaInstancia.setValue(2, estado);
            nuevaInstancia.setValue(3, interes);
            nuevaInstancia.setValue(4, tiempo);

            double prediccion = modelo.classifyInstance(nuevaInstancia);
            return estructura.classAttribute().value((int) prediccion);

        } catch (Exception e) {
            System.err.println("❌ Error en predicción: " + e.getMessage());
            return "Error";
        }
    }

    public double obtenerProbabilidadSi(double citas, double antiguedad, String estado, String interes, double tiempo) {
        try {
            Instance nuevaInstancia = new weka.core.DenseInstance(estructura.numAttributes());
            nuevaInstancia.setDataset(estructura);

            nuevaInstancia.setValue(0, citas);
            nuevaInstancia.setValue(1, antiguedad);
            nuevaInstancia.setValue(2, estado);
            nuevaInstancia.setValue(3, interes);
            nuevaInstancia.setValue(4, tiempo);

            double[] distribucion = modelo.distributionForInstance(nuevaInstancia);
            return distribucion[0] * 100;

        } catch (Exception e) {
            return 50.0;
        }
    }

    @Override
    public Usuario aplicarPrediccionYActualizar(Usuario usuario) {
        try {
            double citas = usuario.getCantidadCitas() != null ? usuario.getCantidadCitas() : 0;
            double antiguedad = usuario.getAntiguedadCuenta() != null ? usuario.getAntiguedadCuenta() : 0;
            String estado = usuario.getEstadoUltimaCita() != null ? usuario.getEstadoUltimaCita() : "Pendiente";
            String interes = usuario.getInteresVehiculo() != null ? usuario.getInteresVehiculo() : "No";
            double tiempo = usuario.getTiempoEntreCitas() != null ? usuario.getTiempoEntreCitas() : 0;

            String prediccion = predecir(citas, antiguedad, estado, interes, tiempo);
            double probabilidad = obtenerProbabilidadSi(citas, antiguedad, estado, interes, tiempo);

            usuario.setClientePotencial(prediccion);
            usuario.setProbabilidad(probabilidad);
            usuario.setObservaciones(generarObservaciones(prediccion, probabilidad, citas, estado));

            return usuario;
        } catch (Exception e) {
            usuario.setClientePotencial("No");
            usuario.setProbabilidad(0.0);
            usuario.setObservaciones("Error en análisis predictivo");
            return usuario;
        }
    }

    @Override
    public String generarObservaciones(String prediccion, double probabilidad, double citas, String estado) {
        StringBuilder observaciones = new StringBuilder();
        if ("Si".equals(prediccion)) {
            observaciones.append("Cliente potencial identificado. ");
        } else {
            observaciones.append("Requiere seguimiento adicional. ");
        }
        observaciones.append("Probabilidad: ").append(String.format("%.1f", probabilidad)).append("%. ");
        if (citas == 0) {
            observaciones.append("Sin citas previas. ");
        } else if (citas >= 3) {
            observaciones.append("Alto nivel de interés demostrado. ");
        }
        if ("Completada".equals(estado)) {
            observaciones.append("Última cita completada exitosamente.");
        } else if ("Cancelada".equals(estado)) {
            observaciones.append("Última cita cancelada.");
        }
        return observaciones.toString();
    }
}