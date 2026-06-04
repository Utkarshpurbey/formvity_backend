package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.FormPublicationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public respondent-facing endpoints (no auth).
 */
@RestController
@AllArgsConstructor
@RequestMapping("/public/forms")
public class PublicFormsController {

    private final FormPublicationService formPublicationService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<?>> getPublishedForm(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(formPublicationService.getPublishedPageDef(slug)));
    }
}
