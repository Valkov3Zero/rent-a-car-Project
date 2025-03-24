package org.example.rentacar.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentRequest {

    private UUID rentalId;
    private UUID selectedCardId;
}
