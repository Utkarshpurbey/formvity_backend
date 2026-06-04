package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.WorkSpaceCreateRequest;
import com.example.uttuCodes.formvity.dto.WorkSpaceMemberInputDto;
import com.example.uttuCodes.formvity.dto.WorkSpaceOutputDto;
import com.example.uttuCodes.formvity.dto.WorkspaceCardDto;
import com.example.uttuCodes.formvity.dto.WorkspaceDashboardDto;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;

import java.util.List;
import java.util.UUID;

public interface WorkSpaceService {
    List<WorkSpaceOutputDto> getUserWorkSpace(UUID userId);

    List<WorkspaceCardDto> listWorkspaceCards(UUID userId);

    WorkspaceDashboardDto getWorkspaceDashboard(UUID workspaceId, UUID userId);

    WorkSpacesEntity createWorkSpace(WorkSpaceCreateRequest request, UUID userId);

    WorkSpacesEntity getWorkSpaceMetaData(UUID workSpaceId);

    void deleteWorkspace(UUID workSpaceId);

    List<WorkSpaceMemberInputDto> getMembersList(UUID workSpaceId);
}
