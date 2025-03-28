package org.example.rentacar.email.client;

import org.example.rentacar.email.client.dto.NotificationDate;
import org.example.rentacar.email.client.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "notification-svc",url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @PostMapping
    void sendNotification(@RequestBody NotificationRequest notificationRequest);

    @GetMapping
    ResponseEntity<NotificationDate> getNotificationDate(@RequestParam(name = "userId") UUID userId);
}
