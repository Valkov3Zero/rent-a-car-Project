package org.example.rentacar.web.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;



@Data
public class RegisterRequest {

    @Size(min = 6 , message = "Username must be at least 6 symbols ")
    private String username;

    @Email
    private String email;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @Size(min = 10)
    private String phoneNumber;

    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;


}
