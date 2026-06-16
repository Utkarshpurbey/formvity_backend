package com.example.uttuCodes.formvity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivateUserDto {

    @NotBlank
    private String token;

    @NotBlank
    private String displayName;

    @NotBlank
    @Size(min = 8)
    private String password;
}
