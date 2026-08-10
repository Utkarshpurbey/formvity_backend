package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.TagCreateRequestDto;
import com.example.uttuCodes.formvity.dto.TagResponseDto;
import com.example.uttuCodes.formvity.dto.TagUpdateRequestDto;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import com.example.uttuCodes.formvity.entity.SubmissionTagEntity;
import com.example.uttuCodes.formvity.entity.TagsEntity;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.enums.TagSource;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.SubmissionRepository;
import com.example.uttuCodes.formvity.repository.SubmissionTagRepository;
import com.example.uttuCodes.formvity.repository.TagsRepository;
import com.example.uttuCodes.formvity.repository.WorkSpaceRepository;
import com.example.uttuCodes.formvity.service.TagService;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagsRepository tagsRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionTagRepository submissionTagRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Override
    @Transactional
    public TagResponseDto createTag(UUID workspaceId, TagCreateRequestDto dto, UUID userId) {
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);

        WorkSpacesEntity workspace = workSpaceRepository.findByWorkSpaceId(workspaceId);
        if (workspace == null) {
            throw FormvityException.notFound("Workspace not found with id: " + workspaceId);
        }

        String name = dto.getName().trim();
        Optional<TagsEntity> existingOpt = tagsRepository.findByWorkSpace_WorkSpaceIdAndName(workspaceId, name);

        TagsEntity tag;
        if (existingOpt.isPresent()) {
            tag = existingOpt.get();
            if (tag.isActive()) {
                throw FormvityException.conflict("Tag with name '" + name + "' already exists in this workspace");
            }
            // Reactivate soft-deleted tag
            tag.setActive(true);
            if (dto.getHexCode() != null && !dto.getHexCode().isBlank()) {
                tag.setHexCode(dto.getHexCode().trim());
            }
        } else {
            tag = new TagsEntity();
            tag.setWorkSpace(workspace);
            tag.setName(name);
            tag.setActive(true);
            if (dto.getHexCode() != null && !dto.getHexCode().isBlank()) {
                tag.setHexCode(dto.getHexCode().trim());
            } else {
                tag.setHexCode("#3B82F6");
            }
        }

        TagsEntity saved = tagsRepository.save(tag);
        log.info("Created tag id={} name={} workspaceId={}", saved.getId(), saved.getName(), workspaceId);
        return TagResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponseDto> getWorkspaceActiveTags(UUID workspaceId, UUID userId) {
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
        List<TagsEntity> tags = tagsRepository.findByWorkSpace_WorkSpaceIdAndActiveTrue(workspaceId);
        return tags.stream()
                .sorted(Comparator.comparing(TagsEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(TagResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponseDto getTagById(UUID tagId, UUID userId) {
        TagsEntity tag = tagsRepository.findByIdAndActiveTrue(tagId)
                .orElseThrow(() -> FormvityException.notFound("Tag not found with id: " + tagId));
        workspaceAccessService.requireUserExistInWorkSpace(userId, tag.getWorkSpace().getWorkSpaceId());
        return TagResponseDto.fromEntity(tag);
    }

    @Override
    @Transactional
    public TagResponseDto updateTag(UUID tagId, TagUpdateRequestDto dto, UUID userId) {
        TagsEntity tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> FormvityException.notFound("Tag not found with id: " + tagId));

        workspaceAccessService.requireUserExistInWorkSpace(userId, tag.getWorkSpace().getWorkSpaceId());

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            if (!newName.equalsIgnoreCase(tag.getName())) {
                boolean duplicateExists = tagsRepository.existsByWorkSpace_WorkSpaceIdAndNameAndActiveTrueAndIdNot(
                        tag.getWorkSpace().getWorkSpaceId(), newName, tagId);
                if (duplicateExists) {
                    throw FormvityException.conflict("Tag with name '" + newName + "' already exists in this workspace");
                }
                tag.setName(newName);
            }
        }

        if (dto.getHexCode() != null && !dto.getHexCode().isBlank()) {
            tag.setHexCode(dto.getHexCode().trim());
        }

        TagsEntity updated = tagsRepository.save(tag);
        log.info("Updated tag id={} name={}", updated.getId(), updated.getName());
        return TagResponseDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteTag(UUID tagId, UUID userId) {
        TagsEntity tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> FormvityException.notFound("Tag not found with id: " + tagId));

        workspaceAccessService.requireUserExistInWorkSpace(userId, tag.getWorkSpace().getWorkSpaceId());

        tag.setActive(false);
        tagsRepository.save(tag);
        log.info("Soft deleted tag id={}", tagId);
    }

    @Override
    @Transactional
    public List<TagResponseDto> attachTagToSubmission(UUID submissionId, UUID tagId, TagSource source, UUID userId) {
        SubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> FormvityException.notFound("Submission not found with id: " + submissionId));

        UUID workspaceId = submission.getForm().getWorkspace().getWorkSpaceId();
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);

        TagsEntity tag = tagsRepository.findByIdAndActiveTrue(tagId)
                .orElseThrow(() -> FormvityException.notFound("Tag not found or inactive with id: " + tagId));

        if (!tag.getWorkSpace().getWorkSpaceId().equals(workspaceId)) {
            throw FormvityException.badRequest("Tag belongs to a different workspace");
        }

        if (!submissionTagRepository.existsBySubmission_IdAndTag_Id(submissionId, tagId)) {
            SubmissionTagEntity submissionTag = new SubmissionTagEntity();
            submissionTag.setSubmission(submission);
            submissionTag.setTag(tag);
            submissionTag.setCreatedBy(userId);
            submissionTag.setSource(source != null ? source : TagSource.MANUAL);
            submissionTagRepository.save(submissionTag);
            log.info("Attached tag id={} (source={}) to submission id={}", tagId, submissionTag.getSource(), submissionId);
        }

        return getSubmissionTags(submissionId, userId);
    }

    @Override
    @Transactional
    public List<TagResponseDto> bulkAttachTagsToSubmission(UUID submissionId, List<UUID> tagIds, TagSource source, UUID userId) {
        SubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> FormvityException.notFound("Submission not found with id: " + submissionId));

        UUID workspaceId = submission.getForm().getWorkspace().getWorkSpaceId();
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);

        TagSource effectiveSource = source != null ? source : TagSource.MANUAL;

        for (UUID tagId : tagIds) {
            Optional<TagsEntity> tagOpt = tagsRepository.findByIdAndActiveTrue(tagId);
            if (tagOpt.isPresent()) {
                TagsEntity tag = tagOpt.get();
                if (tag.getWorkSpace().getWorkSpaceId().equals(workspaceId)) {
                    if (!submissionTagRepository.existsBySubmission_IdAndTag_Id(submissionId, tagId)) {
                        SubmissionTagEntity submissionTag = new SubmissionTagEntity();
                        submissionTag.setSubmission(submission);
                        submissionTag.setTag(tag);
                        submissionTag.setCreatedBy(userId);
                        submissionTag.setSource(effectiveSource);
                        submissionTagRepository.save(submissionTag);
                    }
                }
            }
        }
        log.info("Bulk attached {} tags to submission id={}", tagIds.size(), submissionId);

        return getSubmissionTags(submissionId, userId);
    }

    @Override
    @Transactional
    public List<TagResponseDto> removeTagFromSubmission(UUID submissionId, UUID tagId, UUID userId) {
        SubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> FormvityException.notFound("Submission not found with id: " + submissionId));

        UUID workspaceId = submission.getForm().getWorkspace().getWorkSpaceId();
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);

        submissionTagRepository.deleteBySubmission_IdAndTag_Id(submissionId, tagId);
        log.info("Removed tag id={} from submission id={}", tagId, submissionId);

        return getSubmissionTags(submissionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponseDto> getSubmissionTags(UUID submissionId, UUID userId) {
        SubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> FormvityException.notFound("Submission not found with id: " + submissionId));

        UUID workspaceId = submission.getForm().getWorkspace().getWorkSpaceId();
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);

        List<SubmissionTagEntity> submissionTags = submissionTagRepository.findBySubmission_Id(submissionId);

        return submissionTags.stream()
                .filter(st -> st.getTag() != null && st.getTag().isActive())
                .sorted(Comparator.comparing(st -> st.getTag().getName(), String.CASE_INSENSITIVE_ORDER))
                .map(TagResponseDto::fromSubmissionTag)
                .toList();
    }
}
