package com.example.uttuCodes.formvity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCreateRequestDto {

    @NotBlank(message = "Tag name is required")
    @Size(max = 30, message = "Tag name cannot exceed 30 characters")
    private String name;

    private String hexCode;
}
