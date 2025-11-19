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
     * This keeps the same signature used across your app.
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            // Build JSON payload according to Brevo's /v3/smtp/email API
            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("email", senderEmail);
            sender.put("name", senderName);
            payload.put("sender", sender);

            Map<String, String> to = new HashMap<>();
            to.put("email", toEmail);
            payload.put("to", List.of(to));

            payload.put("subject", subject);
            // You can also use "htmlContent" if you want HTML emails
            payload.put("textContent", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.set("accept", "application/json");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(brevoApiUrl, requestEntity, String.class);

            log.info("Brevo email sent to {} – status: {}", toEmail, response.getStatusCode());

        } catch (RestClientException e) {
            // Do NOT break registration/login if email fails
            log.warn("Failed to send email via Brevo to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email via Brevo to {}", toEmail, e);
        }
    }
}