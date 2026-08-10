package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.TagResponseDto;
import com.example.uttuCodes.formvity.dto.TagUpdateRequestDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.TagService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponseDto>> getTagById(@PathVariable UUID tagId) {
        UUID userId = Utils.getLoggedInUserId();
        TagResponseDto tag = tagService.getTagById(tagId, userId);
        return ResponseEntity.ok(ApiResponse.ok(tag));
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponseDto>> updateTag(
            @PathVariable UUID tagId,
            @RequestBody @Valid TagUpdateRequestDto request) {
        UUID userId = Utils.getLoggedInUserId();
        TagResponseDto updated = tagService.updateTag(tagId, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Tag updated successfully"));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<String>> deleteTag(@PathVariable UUID tagId) {
        UUID userId = Utils.getLoggedInUserId();
        tagService.deleteTag(tagId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Tag " + tagId + " deleted successfully"));
    }
}
