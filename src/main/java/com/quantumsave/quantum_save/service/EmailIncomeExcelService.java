package com.quantumsave.quantum_save.service;

import com.quantumsave.quantum_save.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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

        String filename = "income_details_" + LocalDate.now() + ".xlsx";
        String subject = "Your Income Report (Excel)";
        String body = "Attached is your income report for the current month.\n\n- Quantum Save";

        emailService.sendEmailWithAttachment(toEmail, subject, body, filename, excelBytes);
    }
}
