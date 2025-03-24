package org.example.rentacar.email.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "notification-svc",url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {


}
