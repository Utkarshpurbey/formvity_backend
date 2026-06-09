package com.example.uttuCodes.formvity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormSubmissionInputDto {
    Map<String,Object> respondent;
    Map<String,Object> answers;
    Map<String,Object> metadata;
}
