package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.WorkSpaceCreateRequest;
import com.example.uttuCodes.formvity.dto.WorkSpaceOutputDto;
import com.example.uttuCodes.formvity.dto.WorkspaceCardDto;
import com.example.uttuCodes.formvity.dto.WorkspaceDashboardDto;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;
import com.example.uttuCodes.formvity.enums.FormRoles;
import com.example.uttuCodes.formvity.enums.FormStatus;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.WorkSpaceRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberRepository;
import com.example.uttuCodes.formvity.service.WorkSpaceService;
import com.example.uttuCodes.formvity.utils.Utils;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class WorkSpaceServiceImpl implements WorkSpaceService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final FormRepository formRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WorkSpaceOutputDto> getUserWorkSpace(UUID userId) {
        requireAuthenticatedUser(userId);
        try {
            List<WorkspaceMemberEntity> workspaceMemberEntity = workspaceMemberRepository.findAllByUserId(userId);
            return workspaceMemberEntity.stream()
                    .filter(workspaceMemberEntity1 -> Boolean.TRUE.equals(workspaceMemberEntity1.getActive()))
                    .map(this::toOutputDto).collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.error("Failed to load workspaces", ex);
            throw FormvityException.serviceUnavailable(
                    "Unable to load workspaces. Please try again later.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceCardDto> listWorkspaceCards(UUID userId) {
        requireAuthenticatedUser(userId);
        return getUserWorkSpace(userId).stream()
                .map(ws -> new WorkspaceCardDto(
                        ws.getWorkSpaceId(),
                        ws.getWorkSpaceName(),
                        formRepository.countByWorkspace_WorkSpaceIdAndStatusNot(
                                ws.getWorkSpaceId(), FormStatus.ARCHIVED)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceDashboardDto getWorkspaceDashboard(UUID workspaceId, UUID userId) {
        requireAuthenticatedUser(userId);
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
        WorkSpacesEntity workspace = getWorkSpaceMetaData(workspaceId);
        var forms = formRepository.findActiveByWorkspace(workspaceId);
        return new WorkspaceDashboardDto(
                workspace.getWorkSpaceId(),
                workspace.getWorkSpaceName(),
                forms.size(),
                forms);
    }

    @Override
    @Transactional
    public WorkSpacesEntity createWorkSpace(WorkSpaceCreateRequest request, UUID userId) {
        requireAuthenticatedUser(userId);
        if (request == null || request.getWorkSpaceName() == null || request.getWorkSpaceName().isBlank()) {
            throw FormvityException.badRequest("Workspace name is required");
        }
        try {
            WorkSpaceCreateRequest trimmed = new WorkSpaceCreateRequest();
            trimmed.setWorkSpaceName(request.getWorkSpaceName().trim());

            WorkSpacesEntity workSpacesEntity = modelMapper.map(trimmed, WorkSpacesEntity.class);
            workSpacesEntity.setCreatedByUser(userId);
            workSpacesEntity.setActive(true);
            workSpacesEntity = workSpaceRepository.save(workSpacesEntity);

            WorkspaceMemberEntity member = new WorkspaceMemberEntity();
            member.userId = userId;
            member.workspace = workSpacesEntity;
            member.setActive(true);
            member.setRole(FormRoles.ADMIN);
            workspaceMemberRepository.save(member);
            return workSpacesEntity;
        } catch (DataAccessException ex) {
            log.error("Failed to create workspace", ex);
            throw FormvityException.serviceUnavailable(
                    "Unable to create workspace. Please try again later.", ex);
        }
    }

    @Override
    public WorkSpacesEntity getWorkSpaceMetaData(UUID workSpaceId) {
        try {
            WorkSpacesEntity workSpacesEntity = workSpaceRepository.findByWorkSpaceId(workSpaceId);
            if (workSpacesEntity == null || !workSpacesEntity.isActive()) {
                throw FormvityException.notFound("No active workspace found for id: " + workSpaceId);
            }
            return workSpacesEntity;

        } catch (FormvityException e) {
            throw e;
        } catch (Exception e) {
            throw FormvityException.internalServerError("Unable to load workspace. Please try again later.");
        }
    }

    @Override
    public void deleteWorkspace(UUID workSpaceId) {
        try {
            WorkSpacesEntity workSpacesEntity = getWorkSpaceMetaData(workSpaceId);
            if (workSpacesEntity == null || !workSpacesEntity.isActive()) {
                throw FormvityException.notFound("No active workspace found for id: " + workSpaceId);
            }
            workSpacesEntity.setActive(false);
            workSpaceRepository.save(workSpacesEntity);
        } catch (FormvityException e) {
            throw e;
        } catch (Exception e) {
            throw FormvityException.internalServerError("Unable to delete workspace. Please try again later.");
        }
    }

    @Override
    public WorkSpacesEntity changeWorkspaceName(UUID workspaceId, String workspaceName) {
        UUID userId = Utils.getLoggedInUserId();
        requireAuthenticatedUser(userId);
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);

        if (workspaceName == null || workspaceName.isBlank()) {
            throw FormvityException.badRequest("Workspace name is required");
        }

        WorkSpacesEntity workSpacesEntity = getWorkSpaceMetaData(workspaceId);
        String trimmedName = workspaceName.trim();
        workSpacesEntity.setWorkSpaceName(trimmedName);
        workSpacesEntity.setUpdatedAt(java.time.LocalDateTime.now());
        WorkSpacesEntity saved = workSpaceRepository.save(workSpacesEntity);
        log.info("Name changed to {} for workspaceId {}", trimmedName, workspaceId);
        return saved;
    }

    private static void requireAuthenticatedUser(UUID userId) {
        if (userId == null) {
            throw FormvityException.unauthorized("User not logged in");
        }
    }

    private WorkSpaceOutputDto toOutputDto(WorkspaceMemberEntity member) {
        WorkSpaceOutputDto dto = new WorkSpaceOutputDto();
        if (member.workspace != null) {
            dto.setWorkSpaceId(member.workspace.getWorkSpaceId());
            dto.setWorkSpaceName(member.workspace.getWorkSpaceName());
        }
        return dto;
    }
}
