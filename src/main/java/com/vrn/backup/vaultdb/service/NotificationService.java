package com.vrn.backup.vaultdb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${slack.webhook.url:}")
    private String slackWebhookUrl;

    private final RestTemplate restTemplate;

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendSlackNotification(String message) {
        if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
            return;
        }
        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);
        try {
            restTemplate.postForEntity(slackWebhookUrl, payload, String.class);
        } catch (Exception e) {
            log.error("Failed to send Slack notification", e);
        }
    }
}
