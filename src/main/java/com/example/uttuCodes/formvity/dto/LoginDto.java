package com.example.uttuCodes.formvity.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    /** Display name or email; JSON may use userName, email, username, or user_name. */
    @NotBlank(message = "Login identifier is required")
    @JsonAlias({"email", "username", "user_name"})
    private String userName;

    @NotBlank
    private String password;
}
