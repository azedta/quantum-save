package com.quantumsave.quantum_save.service;

import com.quantumsave.quantum_save.dto.ExpenseDTO;
import com.quantumsave.quantum_save.dto.IncomeDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public byte[] exportCurrentMonthIncomeExcelForCurrentUser() {
        List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Income");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            // ✅ Only the columns you want
            String[] headers = {"ID", "Name", "Category", "Amount", "Date"};

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (IncomeDTO income : incomes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(income.getId() != null ? income.getId() : 0L);
                row.createCell(1).setCellValue(nullSafe(income.getName()));
                row.createCell(2).setCellValue(nullSafe(income.getCategoryName()));

                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(income.getAmount() != null ? income.getAmount().doubleValue() : 0.0);
                amountCell.setCellStyle(amountStyle);

                row.createCell(4).setCellValue(income.getDate() != null ? income.getDate().toString() : "");
            }

            // ✅ Prevent header clipping (this is why you saw "es")
            sheet.setColumnWidth(0, 3500);  // ID
            sheet.setColumnWidth(1, 9000);  // Name
            sheet.setColumnWidth(2, 7000);  // Category
            sheet.setColumnWidth(3, 5000);  // Amount
            sheet.setColumnWidth(4, 5000);  // Date

            // Optional: also auto-size (keeps it clean)
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate income Excel file", e);
        }
    }

    public byte[] exportCurrentMonthExpenseExcelForCurrentUser() {
        List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expense");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            String[] headers = {"ID", "Name", "Category", "Amount", "Date"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ExpenseDTO expense : expenses) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(expense.getId() != null ? expense.getId() : 0L);
                row.createCell(1).setCellValue(nullSafe(expense.getName()));
                row.createCell(2).setCellValue(nullSafe(expense.getCategoryName()));

                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(
                        expense.getAmount() != null ? expense.getAmount().doubleValue() : 0.0
                );
                amountCell.setCellStyle(amountStyle);

                row.createCell(4).setCellValue(
                        expense.getDate() != null ? expense.getDate().toString() : ""
                );
            }

            sheet.setColumnWidth(0, 3500);
            sheet.setColumnWidth(1, 9000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate expense Excel file", e);
        }
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createAmountStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }
}
