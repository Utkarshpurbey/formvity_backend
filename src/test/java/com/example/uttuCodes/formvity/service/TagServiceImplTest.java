package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.TagCreateRequestDto;
import com.example.uttuCodes.formvity.dto.TagResponseDto;
import com.example.uttuCodes.formvity.dto.TagUpdateRequestDto;
import com.example.uttuCodes.formvity.entity.FormEntity;
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
import com.example.uttuCodes.formvity.service.impl.TagServiceImpl;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagsRepository tagsRepository;

    @Mock
    private WorkSpaceRepository workSpaceRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionTagRepository submissionTagRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @InjectMocks
    private TagServiceImpl tagService;

    private UUID workspaceId;
    private UUID userId;
    private UUID tagId;
    private UUID submissionId;
    private WorkSpacesEntity workspace;
    private SubmissionEntity submission;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        userId = UUID.randomUUID();
        tagId = UUID.randomUUID();
        submissionId = UUID.randomUUID();

        workspace = new WorkSpacesEntity();
        workspace.setWorkSpaceId(workspaceId);
        workspace.setWorkSpaceName("Test Workspace");

        FormEntity form = new FormEntity();
        form.setId(UUID.randomUUID());
        form.setWorkspace(workspace);

        submission = new SubmissionEntity();
        submission.setId(submissionId);
        submission.setForm(form);
    }

    @Test
    void createTag_Success() {
        TagCreateRequestDto dto = new TagCreateRequestDto("Bug", "#3B82F6");

        when(workSpaceRepository.findByWorkSpaceId(workspaceId)).thenReturn(workspace);
        when(tagsRepository.findByWorkSpace_WorkSpaceIdAndName(workspaceId, "Bug")).thenReturn(Optional.empty());
        when(tagsRepository.save(any(TagsEntity.class))).thenAnswer(invocation -> {
            TagsEntity entity = invocation.getArgument(0);
            entity.setId(tagId);
            return entity;
        });

        TagResponseDto response = tagService.createTag(workspaceId, dto, userId);

        assertNotNull(response);
        assertEquals(tagId, response.getId());
        assertEquals("Bug", response.getName());
        assertEquals("#3B82F6", response.getHexCode());
        verify(workspaceAccessService).requireUserExistInWorkSpace(userId, workspaceId);
    }

    @Test
    void createTag_DuplicateName_ThrowsConflict() {
        TagCreateRequestDto dto = new TagCreateRequestDto("Bug", "#3B82F6");

        TagsEntity existing = new TagsEntity();
        existing.setId(tagId);
        existing.setName("Bug");
        existing.setWorkSpace(workspace);
        existing.setActive(true);

        when(workSpaceRepository.findByWorkSpaceId(workspaceId)).thenReturn(workspace);
        when(tagsRepository.findByWorkSpace_WorkSpaceIdAndName(workspaceId, "Bug")).thenReturn(Optional.of(existing));

        assertThrows(FormvityException.class, () -> tagService.createTag(workspaceId, dto, userId));
    }

    @Test
    void createTag_ReactivateSoftDeletedTag() {
        TagCreateRequestDto dto = new TagCreateRequestDto("Bug", "#EF4444");

        TagsEntity inactive = new TagsEntity();
        inactive.setId(tagId);
        inactive.setName("Bug");
        inactive.setWorkSpace(workspace);
        inactive.setActive(false);

        when(workSpaceRepository.findByWorkSpaceId(workspaceId)).thenReturn(workspace);
        when(tagsRepository.findByWorkSpace_WorkSpaceIdAndName(workspaceId, "Bug")).thenReturn(Optional.of(inactive));
        when(tagsRepository.save(any(TagsEntity.class))).thenAnswer(i -> i.getArgument(0));

        TagResponseDto response = tagService.createTag(workspaceId, dto, userId);

        assertNotNull(response);
        assertEquals("Bug", response.getName());
        assertEquals("#EF4444", response.getHexCode());
        assertTrue(inactive.isActive());
    }

    @Test
    void getWorkspaceActiveTags_Success() {
        TagsEntity tag1 = new TagsEntity(UUID.randomUUID(), workspace, "Bug", "#3B82F6", null, null, true);
        TagsEntity tag2 = new TagsEntity(UUID.randomUUID(), workspace, "Feature", "#10B981", null, null, true);

        when(tagsRepository.findByWorkSpace_WorkSpaceIdAndActiveTrue(workspaceId)).thenReturn(List.of(tag1, tag2));

        List<TagResponseDto> result = tagService.getWorkspaceActiveTags(workspaceId, userId);

        assertEquals(2, result.size());
        assertEquals("Bug", result.get(0).getName());
        assertEquals("Feature", result.get(1).getName());
    }

    @Test
    void getTagById_Success() {
        TagsEntity tag = new TagsEntity(tagId, workspace, "Bug", "#3B82F6", null, null, true);

        when(tagsRepository.findByIdAndActiveTrue(tagId)).thenReturn(Optional.of(tag));

        TagResponseDto response = tagService.getTagById(tagId, userId);

        assertNotNull(response);
        assertEquals("Bug", response.getName());
        verify(workspaceAccessService).requireUserExistInWorkSpace(userId, workspaceId);
    }

    @Test
    void updateTag_Success() {
        TagsEntity tag = new TagsEntity(tagId, workspace, "Bug", "#3B82F6", null, null, true);
        TagUpdateRequestDto dto = new TagUpdateRequestDto("Critical Bug", "#EF4444");

        when(tagsRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(tagsRepository.existsByWorkSpace_WorkSpaceIdAndNameAndActiveTrueAndIdNot(workspaceId, "Critical Bug", tagId))
                .thenReturn(false);
        when(tagsRepository.save(any(TagsEntity.class))).thenAnswer(i -> i.getArgument(0));

        TagResponseDto response = tagService.updateTag(tagId, dto, userId);

        assertEquals("Critical Bug", response.getName());
        assertEquals("#EF4444", response.getHexCode());
    }

    @Test
    void deleteTag_SoftDeleteSuccess() {
        TagsEntity tag = new TagsEntity(tagId, workspace, "Bug", "#3B82F6", null, null, true);

        when(tagsRepository.findById(tagId)).thenReturn(Optional.of(tag));

        tagService.deleteTag(tagId, userId);

        assertFalse(tag.isActive());
        verify(tagsRepository).save(tag);
    }

    @Test
    void attachTagToSubmission_Success() {
        TagsEntity tag = new TagsEntity(tagId, workspace, "Bug", "#3B82F6", null, null, true);
        SubmissionTagEntity subTag = new SubmissionTagEntity(UUID.randomUUID(), submission, tag, userId, TagSource.MANUAL, null, null, null);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(tagsRepository.findByIdAndActiveTrue(tagId)).thenReturn(Optional.of(tag));
        when(submissionTagRepository.existsBySubmission_IdAndTag_Id(submissionId, tagId)).thenReturn(false);
        when(submissionTagRepository.save(any(SubmissionTagEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(submissionTagRepository.findBySubmission_Id(submissionId)).thenReturn(List.of(subTag));

        List<TagResponseDto> tags = tagService.attachTagToSubmission(submissionId, tagId, TagSource.MANUAL, userId);

        assertEquals(1, tags.size());
        assertEquals("Bug", tags.get(0).getName());
        assertEquals(TagSource.MANUAL, tags.get(0).getSource());
    }

    @Test
    void bulkAttachTagsToSubmission_Success() {
        UUID tagId2 = UUID.randomUUID();
        TagsEntity tag1 = new TagsEntity(tagId, workspace, "Bug", "#3B82F6", null, null, true);
        TagsEntity tag2 = new TagsEntity(tagId2, workspace, "Feature", "#10B981", null, null, true);

        SubmissionTagEntity subTag1 = new SubmissionTagEntity(UUID.randomUUID(), submission, tag1, userId, TagSource.AI, null, null, null);
        SubmissionTagEntity subTag2 = new SubmissionTagEntity(UUID.randomUUID(), submission, tag2, userId, TagSource.AI, null, null, null);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(tagsRepository.findByIdAndActiveTrue(tagId)).thenReturn(Optional.of(tag1));
        when(tagsRepository.findByIdAndActiveTrue(tagId2)).thenReturn(Optional.of(tag2));
        when(submissionTagRepository.findBySubmission_Id(submissionId)).thenReturn(List.of(subTag1, subTag2));

        List<TagResponseDto> result = tagService.bulkAttachTagsToSubmission(submissionId, List.of(tagId, tagId2), TagSource.AI, userId);

        assertEquals(2, result.size());
        assertEquals("Bug", result.get(0).getName());
        assertEquals(TagSource.AI, result.get(0).getSource());
        assertEquals("Feature", result.get(1).getName());
        assertEquals(TagSource.AI, result.get(1).getSource());
    }

    @Test
    void attachTagToSubmission_DifferentWorkspace_ThrowsBadRequest() {
        WorkSpacesEntity otherWorkspace = new WorkSpacesEntity();
        otherWorkspace.setWorkSpaceId(UUID.randomUUID());

        TagsEntity tagFromOtherWorkspace = new TagsEntity(tagId, otherWorkspace, "Bug", "#3B82F6", null, null, true);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(tagsRepository.findByIdAndActiveTrue(tagId)).thenReturn(Optional.of(tagFromOtherWorkspace));

        assertThrows(FormvityException.class, () -> tagService.attachTagToSubmission(submissionId, tagId, TagSource.MANUAL, userId));
    }
}
