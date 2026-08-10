package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.analytics.FormTagAnalyticsSummaryDto;
import com.example.uttuCodes.formvity.dto.analytics.SubmissionListItemDto;
import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import com.example.uttuCodes.formvity.entity.SubmissionTagEntity;
import com.example.uttuCodes.formvity.entity.TagsEntity;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.enums.TagSource;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.SubmissionRepository;
import com.example.uttuCodes.formvity.repository.SubmissionTagRepository;
import com.example.uttuCodes.formvity.service.impl.FormAnalyticsServiceImpl;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.uttuCodes.formvity.repository.TagsRepository;

@ExtendWith(MockitoExtension.class)
class FormAnalyticsServiceImplTest {

    @Mock
    private FormRepository formRepository;

    @Mock
    private FormPublicationRepository formPublicationRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionTagRepository submissionTagRepository;

    @Mock
    private TagsRepository tagsRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @InjectMocks
    private FormAnalyticsServiceImpl formAnalyticsService;

    private UUID workspaceId;
    private UUID formId;
    private UUID userId;
    private FormEntity form;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        formId = UUID.randomUUID();
        userId = UUID.randomUUID();

        WorkSpacesEntity workspace = new WorkSpacesEntity();
        workspace.setWorkSpaceId(workspaceId);

        form = new FormEntity();
        form.setId(formId);
        form.setWorkspace(workspace);
        form.setDraftPageDef(Map.of());
    }

    @Test
    void getTagAnalytics_ShouldCalculateDistributionAndSourceCounts() {
        SubmissionEntity s1 = new SubmissionEntity();
        s1.setId(UUID.randomUUID());
        s1.setCreatedAt(LocalDateTime.now());

        SubmissionEntity s2 = new SubmissionEntity();
        s2.setId(UUID.randomUUID());
        s2.setCreatedAt(LocalDateTime.now());

        TagsEntity tag1 = new TagsEntity(UUID.randomUUID(), null, "Bug", "#3B82F6", null, null, true);

        SubmissionTagEntity st1 = new SubmissionTagEntity(UUID.randomUUID(), s1, tag1, userId, TagSource.MANUAL, null, null, null);
        SubmissionTagEntity st2 = new SubmissionTagEntity(UUID.randomUUID(), s2, tag1, userId, TagSource.AI, null, null, null);

        when(formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)).thenReturn(Optional.of(form));
        when(submissionRepository.findAllWithPublicationByFormId(formId)).thenReturn(List.of(s1, s2));
        when(submissionTagRepository.findBySubmission_Form_Id(formId)).thenReturn(List.of(st1, st2));
        when(tagsRepository.findByWorkSpace_WorkSpaceIdAndActiveTrue(workspaceId)).thenReturn(List.of(tag1));

        FormTagAnalyticsSummaryDto summary = formAnalyticsService.getTagAnalytics(workspaceId, formId, userId, 7);

        assertNotNull(summary);
        assertEquals(2, summary.getTotalSubmissions());
        assertEquals(2, summary.getTotalTaggedSubmissions());
        assertEquals(0, summary.getTotalUntaggedSubmissions());
        assertEquals(2, summary.getTotalTagAssignments());
        assertEquals(1, summary.getTags().size());

        var tagDto = summary.getTags().get(0);
        assertEquals("Bug", tagDto.getName());
        assertEquals(2, tagDto.getTaggedSubmissionsCount());
        assertEquals(100.0, tagDto.getPercentage());
        assertEquals(1, tagDto.getManualCount());
        assertEquals(1, tagDto.getAiCount());
    }

    @Test
    void listSubmissions_WithTagFilter_ShouldQueryFilteredSubmissions() {
        UUID tagId = UUID.randomUUID();
        SubmissionEntity s1 = new SubmissionEntity();
        s1.setId(UUID.randomUUID());
        s1.setCreatedAt(LocalDateTime.now());
        s1.setAnswers(Map.of());

        when(formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)).thenReturn(Optional.of(form));
        when(submissionRepository.findByFormIdAndTagIdOrderByCreatedAtDesc(eq(formId), eq(tagId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(s1)));

        Page<SubmissionListItemDto> page = formAnalyticsService.listSubmissions(workspaceId, formId, userId, tagId, 0, 20);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        verify(submissionRepository).findByFormIdAndTagIdOrderByCreatedAtDesc(eq(formId), eq(tagId), any(Pageable.class));
    }
}
