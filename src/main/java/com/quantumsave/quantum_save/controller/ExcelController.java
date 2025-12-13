package com.quantumsave.quantum_save.controller;

import com.quantumsave.quantum_save.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/excel")
public class ExcelController {

    private final ExcelExportService excelExportService;

    @GetMapping("/download/income")
    public ResponseEntity<byte[]> downloadIncomeExcel() {

        byte[] fileBytes = excelExportService.exportCurrentMonthIncomeExcelForCurrentUser();

        String filename = "income_details_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(fileBytes);
    }

    @GetMapping("/download/expense")
    public ResponseEntity<byte[]> downloadExpenseExcel() {

        byte[] fileBytes =
                excelExportService.exportCurrentMonthExpenseExcelForCurrentUser();

        String filename = "expense_details_" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(fileBytes);
    }

}
