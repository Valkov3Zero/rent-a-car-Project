package org.example.rentacar.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rentacar.car.model.Status;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarCreateRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    private int year;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Price is required")
    @Min(value = 0,message = "Price must be positive")
    private BigDecimal pricePerDay;

    @NotNull(message = "Status is required")
    private Status status;

    private String imageUrl;

    private String description;
}
