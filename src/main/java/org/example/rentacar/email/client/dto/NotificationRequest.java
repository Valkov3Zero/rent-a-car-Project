package org.example.rentacar.email.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationRequest {
    private UUID userId;
    private String userEmail;
    private String subject;
    private String body;
}
