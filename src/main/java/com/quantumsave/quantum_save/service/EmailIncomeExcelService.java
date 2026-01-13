package com.quantumsave.quantum_save.service;

import com.quantumsave.quantum_save.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailIncomeExcelService {

    private final ExcelExportService excelExportService;
    private final ProfileService profileService;
    private final EmailService emailService;

    public void sendIncomeExcelToCurrentUser() {
        ProfileEntity user = profileService.getCurrentProfile();

        String toEmail = user.getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            throw new RuntimeException("User email not found");
        }

        byte[] excelBytes = excelExportService.exportCurrentMonthIncomeExcelForCurrentUser();

        LocalDate now = LocalDate.now();
        String monthLabel = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + now.getYear();

        String filename = "income_report_" + now + ".xlsx";

        emailService.sendIncomeReportEmail(
                toEmail,
                user.getFullName(),
                monthLabel,
                filename,
                excelBytes
        );
    }
}
