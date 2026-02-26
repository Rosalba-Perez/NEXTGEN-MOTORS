package com.concesionario.service.interfaces;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface IProspectoReportService {
    org.apache.poi.ss.usermodel.Workbook generarReporteRendimientoMensual() throws IOException;
}
