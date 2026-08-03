package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.TemplateInfoDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplatesController {

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<TemplateInfoDto>>> getWorkspaceTemplates(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.ok(Collections.emptyList()));
    }
}

