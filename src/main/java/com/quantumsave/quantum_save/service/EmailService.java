package com.quantumsave.quantum_save.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    /**
     * Sends a plain-text email via Brevo HTTP API.
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

            log.info("Brevo email sent to {} – status: {}", toEmail, response.getStatusCode());

        } catch (RestClientException e) {
            log.warn("Failed to send email via Brevo to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email via Brevo to {}", toEmail, e);
        }
    }

    /**
     * Sends an email with ONE attachment via Brevo HTTP API.
     * Attachment content must be Base64.
     */
    public void sendEmailWithAttachment(
            String toEmail,
            String subject,
            String body,
            String filename,
            byte[] fileBytes
    ) {
        try {
            Map<String, Object> payload = basePayload(toEmail, subject);
            payload.put("textContent", body);

            String base64 = Base64.getEncoder().encodeToString(fileBytes);

            Map<String, Object> attachment = new HashMap<>();
            attachment.put("name", filename);
            attachment.put("content", base64);

            // Brevo expects an array of attachments
            payload.put("attachment", List.of(attachment));

            ResponseEntity<String> response = restTemplate.postForEntity(
                    brevoApiUrl,
                    new HttpEntity<>(payload, buildHeaders()),
                    String.class
            );

            log.info("Brevo email with attachment sent to {} – status: {}", toEmail, response.getStatusCode());

        } catch (RestClientException e) {
            log.warn("Failed to send email with attachment via Brevo to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email with attachment via Brevo to {}", toEmail, e);
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
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");
        return headers;
    }
}
