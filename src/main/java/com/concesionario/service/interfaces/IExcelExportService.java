package com.concesionario.service.interfaces;

import org.apache.poi.ss.usermodel.Workbook;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface IExcelExportService {
    ByteArrayInputStream exportToExcel(String sheetName, String[] headers,
            java.util.List<java.util.Map<String, Object>> data) throws IOException;

    org.apache.poi.ss.usermodel.CellStyle crearEstiloHeader(org.apache.poi.ss.usermodel.Workbook workbook);

    org.apache.poi.ss.usermodel.CellStyle crearEstiloTitulo(org.apache.poi.ss.usermodel.Workbook workbook);

    org.apache.poi.ss.usermodel.CellStyle crearEstiloDatos(org.apache.poi.ss.usermodel.Workbook workbook);

    org.apache.poi.ss.usermodel.CellStyle crearEstiloVentas(org.apache.poi.ss.usermodel.Workbook workbook);

    org.apache.poi.ss.usermodel.CellStyle crearEstiloPorcentaje(org.apache.poi.ss.usermodel.Workbook workbook);

    org.apache.poi.ss.usermodel.CellStyle crearEstiloTotales(org.apache.poi.ss.usermodel.Workbook workbook);
}
