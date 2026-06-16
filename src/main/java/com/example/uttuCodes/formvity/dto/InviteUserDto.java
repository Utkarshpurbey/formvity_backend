package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.FormRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteUserDto {
    @NotNull
    @Email
    private String email;
    @NotNull
    private FormRoles role;
}
