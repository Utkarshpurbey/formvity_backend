package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.TagCreateRequestDto;
import com.example.uttuCodes.formvity.dto.TagResponseDto;
import com.example.uttuCodes.formvity.dto.TagUpdateRequestDto;
import com.example.uttuCodes.formvity.enums.TagSource;

import java.util.List;
import java.util.UUID;

public interface TagService {

    TagResponseDto createTag(UUID workspaceId, TagCreateRequestDto dto, UUID userId);

    List<TagResponseDto> getWorkspaceActiveTags(UUID workspaceId, UUID userId);

    TagResponseDto getTagById(UUID tagId, UUID userId);

    TagResponseDto updateTag(UUID tagId, TagUpdateRequestDto dto, UUID userId);

    void deleteTag(UUID tagId, UUID userId);

    List<TagResponseDto> attachTagToSubmission(UUID submissionId, UUID tagId, TagSource source, UUID userId);

    List<TagResponseDto> bulkAttachTagsToSubmission(UUID submissionId, List<UUID> tagIds, TagSource source, UUID userId);

    List<TagResponseDto> removeTagFromSubmission(UUID submissionId, UUID tagId, UUID userId);

    List<TagResponseDto> getSubmissionTags(UUID submissionId, UUID userId);
}
