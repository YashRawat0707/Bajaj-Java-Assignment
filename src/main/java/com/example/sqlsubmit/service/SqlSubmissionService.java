package com.example.sqlsubmit.service;

import com.example.sqlsubmit.model.WebhookResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SqlSubmissionService {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void run() {
       
        String initUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = Map.of(
                "name", "Yash Rawat",
                "regNo", "59",
                "email", "yash2559.be22@chitkara.edu.in"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
                initUrl,
                requestEntity,
                WebhookResponse.class
        );

        WebhookResponse webhookResponse = response.getBody();
        if (webhookResponse == null || webhookResponse.getWebhook() == null) {
            System.out.println("Failed to retrieve webhook or token.");
            return;
        }

        String webhookUrl = webhookResponse.getWebhook();
        String accessToken = webhookResponse.getAccessToken();

        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("Access Token: " + accessToken);

       
        String finalQuery = """
                SELECT 
                    p.AMOUNT AS SALARY,
                    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,
                    d.DEPARTMENT_NAME
                FROM PAYMENTS p
                JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                WHERE DAY(p.PAYMENT_TIME) != 1
                ORDER BY p.AMOUNT DESC
                LIMIT 1;
                """;

       
        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        postHeaders.setBearerAuth(accessToken);

        Map<String, String> submissionBody = Map.of("finalQuery", finalQuery);
        HttpEntity<Map<String, String>> submissionRequest = new HttpEntity<>(submissionBody, postHeaders);

        try {
            ResponseEntity<String> submissionResponse = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    submissionRequest,
                    String.class
            );

            System.out.println("Submission Response: " + submissionResponse.getStatusCode());
            System.out.println("Response Body: " + submissionResponse.getBody());
        } catch (Exception e) {
            System.out.println("Submission failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
