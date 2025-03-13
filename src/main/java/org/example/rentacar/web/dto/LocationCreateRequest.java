package org.example.rentacar.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.rentacar.location.model.City;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationCreateRequest {

    @NotBlank(message = "Location name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "City is required")
    private City city;
}
