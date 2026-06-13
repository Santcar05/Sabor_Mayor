package com.sabormayor.analytics.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.analytics.application.AnalyticsService;
import com.sabormayor.analytics.application.ExcelReportBuilder;
import com.sabormayor.analytics.infrastructure.DishSalesFactRepository;
import com.sabormayor.analytics.web.dto.DashboardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Analytics — KPIs and reports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ExcelReportBuilder excelReportBuilder;

    @GetMapping("/dashboard")
    @Operation(summary = "Aggregated KPIs for a date range")
    public DashboardResponse dashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return analyticsService.dashboard(from, to);
    }

    @GetMapping("/top-dishes")
    public List<DishSalesFactRepository.TopDish> topDishes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return analyticsService.topDishes(from, to);
    }

    @GetMapping("/dashboard/export.xlsx")
    @Operation(summary = "Download the dashboard as an Excel workbook")
    public ResponseEntity<byte[]> export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] body = excelReportBuilder.build(analyticsService.dashboard(from, to));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sabor-mayor-report-%s_%s.xlsx\"".formatted(from, to))
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }
}
