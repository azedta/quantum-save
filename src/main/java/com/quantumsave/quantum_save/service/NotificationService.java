package com.quantumsave.quantum_save.service;


import com.quantumsave.quantum_save.dto.ExpenseDTO;
import com.quantumsave.quantum_save.entity.ProfileEntity;
import com.quantumsave.quantum_save.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final  EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${quantum.save.frontend.url}")
    private String frontEndURL;

    //@Scheduled(cron = "0 * * * * *", zone = "EST")
    @Scheduled(cron = "0 0 22 * * *", zone = "EST")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job Started :Sending Daily Income Expense Reminder");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile : profiles) {
            String body =
                    "<div style='font-family:Arial,Helvetica,sans-serif;font-size:15px;color:#0B1533;'>"
                            + "Hi <strong>" + profile.getFullName() + "</strong>,<br><br>"
                            + "<strong>Small habits, big gains.</strong><br>"
                            + "Logging today’s transactions keeps your budget on point and your insights smarter every single day.<br><br>"
                            + "<a href='" + frontEndURL + "' "
                            + "style='display:inline-block;padding:12px 20px;"
                            + "background-color:#00E6C3;color:#0B1533;text-decoration:none;"
                            + "border-radius:8px;font-weight:bold;'>Open Quantum Save</a>"
                            + "<br><br>"
                            + "<span style='font-size:13px;color:#666;'>Takes only a few seconds — but keeps your progress perfectly aligned.</span>"
                            + "<br><br>"
                            + "Best regards,<br>"
                            + "<strong>The Quantum Save Team</strong>"
                            + "</div>";

            emailService.sendEmail(profile.getEmail(), "Quantum Save - Activate your streak: log today’s income & expenses \uD83D\uDCB8", body);
        }
        log.info("Job Completed : Sending Daily Income Expense Reminder");
    }

    //@Scheduled(cron = "0 * * * * *", zone = "EST")
    @Scheduled(cron = "0 0 23 * * *", zone = "EST")
    public void sendDailyExpenseSummary() {
        log.info("Job Started :Sending Daily Expense Summary");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile : profiles) {
            List<ExpenseDTO> todayExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            if(!todayExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();

                table.append("<table style='border-collapse:collapse;width:100%;font-family:Arial,Helvetica,sans-serif;'>");
                table.append("<tr style='background-color:#F5F7FB;color:#0B1533;'>"
                        + "<th style='border:1px solid #ddd;padding:8px;text-align:left;'>#</th>"
                        + "<th style='border:1px solid #ddd;padding:8px;text-align:left;'>Name</th>"
                        + "<th style='border:1px solid #ddd;padding:8px;text-align:left;'>Amount</th>"
                        + "<th style='border:1px solid #ddd;padding:8px;text-align:left;'>Category</th>"
                        + "</tr>");

                int i = 1;
                for (ExpenseDTO expenseDTO : todayExpenses) {
                    table.append("<tr>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expenseDTO.getName()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;color:#0B1533;font-weight:600;'>$")
                            .append(expenseDTO.getAmount()).append("</td>")
                            .append("<td style='border:1px solid #ddd;padding:8px;'>")
                            .append(expenseDTO.getCategoryId() != null ? expenseDTO.getCategoryName() : "N/A")
                            .append("</td>")
                            .append("</tr>");
                }

                table.append("</table>");
                String body =
                        "<div style='font-family:Arial,Helvetica,sans-serif;font-size:15px;color:#0B1533;'>"
                                + "Hi <strong>" + profile.getFullName() + "</strong>,<br/><br/>"
                                + "Here is your expense summary for today:<br/><br/>"
                                + table
                                + "<br/><br/>"
                                + "<a href='" + frontEndURL + "' "
                                + "style='display:inline-block;padding:10px 18px;"
                                + "background-color:#00E6C3;color:#0B1533;text-decoration:none;"
                                + "border-radius:8px;font-weight:bold;'>"
                                + "View Daily Expenses"
                                + "</a>"
                                + "<br/><br/>"
                                + "Best regards,<br/>"
                                + "<strong>Quantum Save Team</strong>"
                                + "</div>";
                emailService.sendEmail(profile.getEmail(), "Today’s Expense Summary – Quantum Save", body);
            }
        }
        log.info("Job Completed :Sending Daily Expense Summary");
    }
}
