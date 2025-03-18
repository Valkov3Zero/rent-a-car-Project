package org.example.rentacar.web.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentalCreateRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Pickup location is required")
    private Long pickupLocationId;

    @NotNull(message = "Dropoff location is required")
    private Long dropoffLocationId;

    @NotNull
    private LocalDate createAt;
}
