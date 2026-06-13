package com.sabormayor.analytics.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.sabormayor.analytics.web.dto.DashboardResponse;

/** Renders the dashboard read model as a downloadable .xlsx report. */
@Component
public class ExcelReportBuilder {

    public byte[] build(DashboardResponse dashboard) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildSummarySheet(workbook, dashboard);
            buildTopDishesSheet(workbook, dashboard);
            buildDailySheet(workbook, dashboard);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not build Excel report", e);
        }
    }

    private void buildSummarySheet(Workbook workbook, DashboardResponse d) {
        Sheet sheet = workbook.createSheet("Resumen");
        addRow(sheet, 0, "Periodo", d.from() + " a " + d.to());
        addRow(sheet, 1, "Ventas totales", d.totalRevenue());
        addRow(sheet, 2, "Pedidos", d.totalOrders());
        addRow(sheet, 3, "Clientes distintos", d.distinctCustomers());
        addRow(sheet, 4, "Ticket promedio", d.averageTicket());
        addRow(sheet, 5, "Reservas", d.totalReservations());
        addRow(sheet, 6, "Reservas canceladas", d.cancelledReservations());
        addRow(sheet, 7, "Tasa no-show", d.noShowRate());
    }

    private void buildTopDishesSheet(Workbook workbook, DashboardResponse d) {
        Sheet sheet = workbook.createSheet("Platos top");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Plato");
        header.createCell(1).setCellValue("Cantidad");
        header.createCell(2).setCellValue("Ingresos");
        int r = 1;
        for (DashboardResponse.TopDish dish : d.topDishes()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(dish.dishName());
            row.createCell(1).setCellValue(dish.quantity());
            row.createCell(2).setCellValue(dish.revenue().doubleValue());
        }
    }

    private void buildDailySheet(Workbook workbook, DashboardResponse d) {
        Sheet sheet = workbook.createSheet("Ventas diarias");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Dia");
        header.createCell(1).setCellValue("Ingresos");
        header.createCell(2).setCellValue("Pedidos");
        int r = 1;
        for (DashboardResponse.DailyPoint point : d.dailyRevenue()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(point.day().toString());
            row.createCell(1).setCellValue(point.revenue().doubleValue());
            row.createCell(2).setCellValue(point.orders());
        }
    }

    private void addRow(Sheet sheet, int rowIdx, String label, Object value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        if (value instanceof Number n) {
            valueCell.setCellValue(n.doubleValue());
        } else {
            valueCell.setCellValue(String.valueOf(value));
        }
    }
}
