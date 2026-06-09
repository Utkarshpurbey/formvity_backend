package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.FormSubmissionInputDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.FormPublicationService;
import com.example.uttuCodes.formvity.service.SubmissionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public respondent-facing endpoints (no auth).
 */
@RestController
@AllArgsConstructor
@RequestMapping("/public/forms")
public class PublicFormsController {

    private final FormPublicationService formPublicationService;
    private final SubmissionService submissionService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<?>> getPublishedForm(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(formPublicationService.getPublishedPageDef(slug)));
    }
    @PostMapping("/{slug}/submit")
    public ResponseEntity<ApiResponse<?>> submit(
            @PathVariable String slug,
            @RequestBody FormSubmissionInputDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(submissionService.submitAnswer(dto, slug)));
    }
}
