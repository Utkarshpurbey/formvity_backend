package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.FormSubmissionInputDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/workspace/{workspaceId}/form/{formId}")
@AllArgsConstructor
public class FormSubmissionController {
    private final SubmissionService submissionService;
//    @PostMapping("/submit")
//    private ResponseEntity<ApiResponse<?>> submitResponse(@Valid FormSubmissionInputDto formSubmissionInputDto, @PathVariable UUID formId, @PathVariable UUID workSpaceId){
//        return ResponseEntity.ok(ApiResponse.ok(submissionService.submitAnswer(formSubmissionInputDto,workSpaceId,formId)));
//    }
}
