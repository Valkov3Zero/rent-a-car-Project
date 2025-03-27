package org.example.rentacar.email.client;

import org.example.rentacar.email.client.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-svc",url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @PostMapping
    void sendNotification(@RequestBody NotificationRequest notificationRequest);

}
