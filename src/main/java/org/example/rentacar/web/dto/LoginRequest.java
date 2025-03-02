package org.example.rentacar.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {


    @NotNull
    private String username;

    @NotNull
    private String password;
}
