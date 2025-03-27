package org.example.rentacar.email.service;

import lombok.extern.slf4j.Slf4j;
import org.example.rentacar.email.client.NotificationClient;
import org.example.rentacar.email.client.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    @Autowired
    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void sendWelcomeEmail(UUID userId, String email) {
        try {
            log.info("Sending Welcome email to " + email);

            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .userEmail(email)
                    .subject("Welcome to RentACar")
                    .body("Thank you for registering with our service. You can now browse and rent cars from our selection.")
                    .build();

            notificationClient.sendNotification(request);
            log.info("Welcome email notification sent successfully to emailUser: {} ",  email);
        } catch (Exception e) {
            log.error("Error while sending Welcome email to {} error:{}",email, e.getMessage());
        }
    }
}
