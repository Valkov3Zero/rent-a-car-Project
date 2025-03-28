package org.example.rentacar.email.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDate {

    private LocalDateTime createdOn;
}
