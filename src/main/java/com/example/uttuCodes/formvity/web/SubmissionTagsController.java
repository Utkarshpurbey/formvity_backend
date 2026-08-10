package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.TagAttachRequestDto;
import com.example.uttuCodes.formvity.dto.TagBulkAttachRequestDto;
import com.example.uttuCodes.formvity.dto.TagResponseDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.enums.TagSource;
import com.example.uttuCodes.formvity.service.TagService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/submissions/{submissionId}/tags")
public class SubmissionTagsController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> attachTag(
            @PathVariable UUID submissionId,
            @RequestBody @Valid TagAttachRequestDto request) {
        UUID userId = Utils.getLoggedInUserId();
        TagSource source = request.getSource() != null ? request.getSource() : TagSource.MANUAL;
        List<TagResponseDto> tags = tagService.attachTagToSubmission(submissionId, request.getTagId(), source, userId);
        return ResponseEntity.ok(ApiResponse.ok(tags, "Tag attached to submission"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> bulkAttachTags(
            @PathVariable UUID submissionId,
            @RequestBody @Valid TagBulkAttachRequestDto request) {
        UUID userId = Utils.getLoggedInUserId();
        TagSource source = request.getSource() != null ? request.getSource() : TagSource.MANUAL;
        List<TagResponseDto> tags = tagService.bulkAttachTagsToSubmission(submissionId, request.getTagIds(), source, userId);
        return ResponseEntity.ok(ApiResponse.ok(tags, "Tags attached to submission"));
    }

    @PostMapping("/{tagId}")
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> attachTagByPath(
            @PathVariable UUID submissionId,
            @PathVariable UUID tagId,
            @RequestParam(required = false) TagSource source) {
        UUID userId = Utils.getLoggedInUserId();
        TagSource effectiveSource = source != null ? source : TagSource.MANUAL;
        List<TagResponseDto> tags = tagService.attachTagToSubmission(submissionId, tagId, effectiveSource, userId);
        return ResponseEntity.ok(ApiResponse.ok(tags, "Tag attached to submission"));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> removeTag(
            @PathVariable UUID submissionId,
            @PathVariable UUID tagId) {
        UUID userId = Utils.getLoggedInUserId();
        List<TagResponseDto> tags = tagService.removeTagFromSubmission(submissionId, tagId, userId);
        return ResponseEntity.ok(ApiResponse.ok(tags, "Tag removed from submission"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> getSubmissionTags(
            @PathVariable UUID submissionId) {
        UUID userId = Utils.getLoggedInUserId();
        List<TagResponseDto> tags = tagService.getSubmissionTags(submissionId, userId);
        return ResponseEntity.ok(ApiResponse.ok(tags));
    }
}
