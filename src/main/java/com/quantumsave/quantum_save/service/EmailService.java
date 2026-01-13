package com.quantumsave.quantum_save.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${quantum.save.frontend.url}")
    private String frontendUrl;

    /**
     * Plain-text email (kept for compatibility).
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            Map<String, Object> payload = basePayload(toEmail, subject);
            payload.put("textContent", body);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    brevoApiUrl,
                    new HttpEntity<>(payload, buildHeaders()),
                    String.class
            );

            log.info("Brevo email sent – status: {}", response.getStatusCode());

        } catch (RestClientException e) {
            log.warn("Failed to send email via Brevo: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email via Brevo", e);
        }
    }

    /**
     * HTML email (also includes text fallback).
     */
    public void sendHtmlEmail(String toEmail, String subject, String textBody, String htmlBody) {
        try {
            Map<String, Object> payload = basePayload(toEmail, subject);
            payload.put("textContent", textBody);
            payload.put("htmlContent", htmlBody);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    brevoApiUrl,
                    new HttpEntity<>(payload, buildHeaders()),
                    String.class
            );

            log.info("Brevo HTML email sent – status: {}", response.getStatusCode());

        } catch (RestClientException e) {
            log.warn("Failed to send HTML email via Brevo: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending HTML email via Brevo", e);
        }
    }

    /**
     * Email with ONE attachment (supports HTML + text fallback).
     * Attachment content must be Base64.
     */
    public void sendEmailWithAttachment(
            String toEmail,
            String subject,
            String textBody,
            String htmlBody,
            String filename,
            byte[] fileBytes
    ) {
        try {
            Map<String, Object> payload = basePayload(toEmail, subject);
            payload.put("textContent", textBody);
            if (htmlBody != null && !htmlBody.isBlank()) payload.put("htmlContent", htmlBody);

            String base64 = Base64.getEncoder().encodeToString(fileBytes);

            Map<String, Object> attachment = new HashMap<>();
            attachment.put("name", filename);
            attachment.put("content", base64);

            payload.put("attachment", List.of(attachment));

            ResponseEntity<String> response = restTemplate.postForEntity(
                    brevoApiUrl,
                    new HttpEntity<>(payload, buildHeaders()),
                    String.class
            );

            log.info("Brevo email with attachment sent – status: {}", response.getStatusCode());

        } catch (RestClientException e) {
            log.warn("Failed to send email with attachment via Brevo: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email with attachment via Brevo", e);
        }
    }

    private Map<String, Object> basePayload(String toEmail, String subject) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("email", senderEmail);
        sender.put("name", senderName);
        payload.put("sender", sender);

        Map<String, String> to = new HashMap<>();
        to.put("email", toEmail);
        payload.put("to", List.of(to));

        payload.put("subject", subject);

        return payload;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        headers.set("api-key", brevoApiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    // ==========================================================
    // ✅ Professional templates
    // ==========================================================

    public void sendActivationEmail(String toEmail, String fullName, String activationLink) {
        String safeName = (fullName == null || fullName.isBlank()) ? "there" : fullName;

        String subject = "Activate your Quantum Save account";

        String text =
                "Hi " + safeName + ",\n\n" +
                        "Welcome to Quantum Save.\n\n" +
                        "Activate your account using this link:\n" +
                        activationLink + "\n\n" +
                        "If you didn’t create this account, you can ignore this email.\n\n" +
                        "— Quantum Save";

        String html = emailShell(
                "Activate your account",
                "Hi " + escape(safeName) + ",",
                "Thanks for signing up for <b>Quantum Save</b>. Please confirm your email address to activate your account.",
                ctaButton("Activate account", activationLink),
                "If the button doesn’t work, copy and paste this link into your browser:<br/>" +
                        "<a href=\"" + activationLink + "\" style=\"color:#4f46e5;\">" + activationLink + "</a>",
                "If you didn’t create this account, you can safely ignore this email."
        );

        sendHtmlEmail(toEmail, subject, text, html);
    }

    public void sendIncomeReportEmail(String toEmail, String fullName, String monthLabel, String filename, byte[] excelBytes) {
        String safeName = (fullName == null || fullName.isBlank()) ? "there" : fullName;

        String subject = "Your Income Report is ready (" + monthLabel + ")";

        String text =
                "Hi " + safeName + ",\n\n" +
                        "Attached is your Income Report for " + monthLabel + ".\n\n" +
                        "Filename: " + filename + "\n\n" +
                        "— Quantum Save";

        String html = emailShell(
                "Income report ready",
                "Hi " + escape(safeName) + ",",
                "Your <b>Income Report</b> for <b>" + escape(monthLabel) + "</b> is attached to this email.",
                "<div style=\"margin-top:10px;font-size:13px;color:#475569;\">File: <b>" + escape(filename) + "</b></div>",
                "Tip: Download the file and keep it for your monthly records.",
                "Need help? Reply to this email and we’ll help you out."
        );

        sendEmailWithAttachment(toEmail, subject, text, html, filename, excelBytes);
    }

    public void sendExpenseReportEmail(String toEmail, String fullName, String monthLabel, String filename, byte[] excelBytes) {
        String safeName = (fullName == null || fullName.isBlank()) ? "there" : fullName;

        String subject = "Your Expense Report is ready (" + monthLabel + ")";

        String text =
                "Hi " + safeName + ",\n\n" +
                        "Attached is your Expense Report for " + monthLabel + ".\n\n" +
                        "Filename: " + filename + "\n\n" +
                        "— Quantum Save";

        String html = emailShell(
                "Expense report ready",
                "Hi " + escape(safeName) + ",",
                "Your <b>Expense Report</b> for <b>" + escape(monthLabel) + "</b> is attached to this email.",
                "<div style=\"margin-top:10px;font-size:13px;color:#475569;\">File: <b>" + escape(filename) + "</b></div>",
                "Tip: Use this report to spot spending patterns and improve your budget.",
                "Need help? Reply to this email and we’ll help you out."
        );

        sendEmailWithAttachment(toEmail, subject, text, html, filename, excelBytes);
    }

    // ==========================================================
    // ✅ HTML building blocks
    // ==========================================================
    private String resolveLogoUrl() {
        if (frontendUrl == null || frontendUrl.isBlank()) return "";
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base.startsWith("http") ? base + "/quantum-save-icon.png" : "";
    }

    private String emailShell(String title, String greeting, String intro, String mainBlock, String note, String footerNote) {
        String year = String.valueOf(Year.now().getValue());
        String computedLogoUrl = resolveLogoUrl();
        String logo = computedLogoUrl.isBlank()
                ? ""
                : "<img src=\"" + computedLogoUrl + "\" alt=\"Quantum Save\" style=\"width:42px;height:42px;border-radius:12px;display:block;\"/>";


        String appLink = (frontendUrl == null || frontendUrl.isBlank()) ? "#" : frontendUrl;

        return ""
                + "<!doctype html>"
                + "<html><head><meta charset=\"utf-8\"/>"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>"
                + "<title>" + escape(title) + "</title>"
                + "</head>"
                + "<body style=\"margin:0;padding:0;background:#f8fafc;font-family:Inter,Arial,sans-serif;color:#0f172a;\">"
                + "  <div style=\"max-width:640px;margin:0 auto;padding:28px 16px;\">"
                + "    <div style=\"background:#ffffff;border:1px solid #e2e8f0;border-radius:18px;overflow:hidden;box-shadow:0 12px 30px rgba(15,23,42,0.06);\">"
                + "      <div style=\"padding:22px 22px 0 22px;display:flex;align-items:center;gap:12px;\">"
                + "        " + logo
                + "        <div>"
                + "          <div style=\"font-weight:700;font-size:15px;letter-spacing:0.18em;color:#64748b;text-transform:uppercase;\">Quantum Save</div>"
                + "          <div style=\"font-size:12px;color:#94a3b8;margin-top:2px;\">Personal finance, reimagined.</div>"
                + "        </div>"
                + "      </div>"
                + "      <div style=\"padding:22px;\">"
                + "        <h1 style=\"margin:0 0 10px 0;font-size:22px;line-height:1.25;\">" + escape(title) + "</h1>"
                + "        <p style=\"margin:0 0 14px 0;font-size:14px;line-height:1.6;color:#334155;\">" + greeting + "</p>"
                + "        <p style=\"margin:0 0 16px 0;font-size:14px;line-height:1.6;color:#334155;\">" + intro + "</p>"
                + "        <div style=\"margin:18px 0;\">" + mainBlock + "</div>"
                + "        <div style=\"font-size:13px;line-height:1.6;color:#475569;background:#f8fafc;border:1px solid #e2e8f0;border-radius:14px;padding:12px;\">"
                + "          " + note
                + "        </div>"
                + "        <p style=\"margin:16px 0 0 0;font-size:12px;line-height:1.6;color:#64748b;\">" + footerNote + "</p>"
                + "      </div>"
                + "      <div style=\"padding:16px 22px;border-top:1px solid #e2e8f0;background:#fbfdff;display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;\">"
                + "        <div style=\"font-size:12px;color:#94a3b8;\">© " + year + " Quantum Save</div>"
                + "        <div style=\"font-size:12px;\"><a href=\"" + appLink + "\" style=\"color:#4f46e5;text-decoration:none;\">Open Quantum Save</a></div>"
                + "      </div>"
                + "    </div>"
                + "  </div>"
                + "</body></html>";
    }

    private String ctaButton(String label, String url) {
        return ""
                + "<a href=\"" + url + "\" "
                + "style=\"display:inline-block;padding:12px 16px;border-radius:12px;"
                + "background:linear-gradient(90deg,#2dd4bf,#34d399,#a78bfa);"
                + "color:#0b1220;text-decoration:none;font-weight:700;font-size:13px;letter-spacing:0.14em;"
                + "text-transform:uppercase;box-shadow:0 10px 25px rgba(56,189,248,0.25);\">"
                + escape(label)
                + "</a>";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

}
