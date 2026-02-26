package com.concesionario.service.interfaces;

import com.concesionario.model.Usuario;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface IReporteService {
    ByteArrayInputStream generarReportePotenciales(List<Usuario> usuarios) throws IOException;
}
