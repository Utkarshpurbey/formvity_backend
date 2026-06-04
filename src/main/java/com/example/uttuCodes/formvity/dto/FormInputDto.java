package com.example.uttuCodes.formvity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormInputDto {

    @NotBlank(message = "title is required")
    @Size(max = 500, message = "title must be at most 500 characters")
    private String title;

    @NotNull(message = "draftPageDef is required")
    private Map<String, Object> draftPageDef;
}
