package com.example.uttuCodes.formvity.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormPatchDto {

    @Size(max = 500, message = "title must be at most 500 characters")
    private String title;

    private Map<String, Object> draftPageDef;
}
