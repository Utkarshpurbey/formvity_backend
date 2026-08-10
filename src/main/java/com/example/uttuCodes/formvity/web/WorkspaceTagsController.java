package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.TagCreateRequestDto;
import com.example.uttuCodes.formvity.dto.TagResponseDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.TagService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces/{workspaceId}/tags")
public class WorkspaceTagsController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponseDto>> createTag(
            @PathVariable UUID workspaceId,
            @RequestBody @Valid TagCreateRequestDto request) {
        UUID userId = Utils.getLoggedInUserId();
        TagResponseDto created = tagService.createTag(workspaceId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Tag created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> getActiveTags(
            @PathVariable UUID workspaceId) {
        UUID userId = Utils.getLoggedInUserId();
        List<TagResponseDto> tags = tagService.getWorkspaceActiveTags(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(tags));
    }
}
