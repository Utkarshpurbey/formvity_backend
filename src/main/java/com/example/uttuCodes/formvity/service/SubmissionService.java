package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.FormSubmissionInputDto;
import com.example.uttuCodes.formvity.dto.SubmitFormResponseDto;

public interface SubmissionService {

    SubmitFormResponseDto submitAnswer(FormSubmissionInputDto formSubmissionInputDto, String slug);
}
