package com.quantumsave.quantum_save.service;

import com.quantumsave.quantum_save.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmailExpenseExcelService {

    private final ExcelExportService excelExportService;
    private final ProfileService profileService;
    private final EmailService emailService;

    public void sendExpenseExcelToCurrentUser() {
        ProfileEntity user = profileService.getCurrentProfile();

        String toEmail = user.getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            throw new RuntimeException("User email not found");
        }

        byte[] excelBytes =
                excelExportService.exportCurrentMonthExpenseExcelForCurrentUser();

        String filename = "expense_details_" + LocalDate.now() + ".xlsx";
        String subject = "Your Expense Report (Excel)";
        String body =
                "Attached is your expense report for the current month.\n\n- Quantum Save";

        emailService.sendEmailWithAttachment(
                toEmail,
                subject,
                body,
                filename,
                excelBytes
        );
    }
}
