package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.FormSubmissionInputDto;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;

import java.util.UUID;

public interface SubmissionService {

    SubmissionEntity submitAnswer(FormSubmissionInputDto formSubmissionInputDto, String slug);
}
