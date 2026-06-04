package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.WorkSpaceCreateRequest;
import com.example.uttuCodes.formvity.dto.WorkSpaceMemberInputDto;
import com.example.uttuCodes.formvity.dto.WorkSpaceOutputDto;
import com.example.uttuCodes.formvity.dto.WorkspaceCardDto;
import com.example.uttuCodes.formvity.dto.WorkspaceDashboardDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.service.WorkSpaceService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkSpaceService workSpaceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceCardDto>>> listWorkspaces() {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(workSpaceService.listWorkspaceCards(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkSpaceOutputDto>> createWorkspace(
            @RequestBody @Valid WorkSpaceCreateRequest request) {
        UUID userId = Utils.getLoggedInUserId();
        WorkSpacesEntity created = workSpaceService.createWorkSpace(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(toOutputDto(created), "Workspace created"));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkSpaceOutputDto>> getWorkspace(@PathVariable UUID workspaceId) {
        WorkSpacesEntity workspace = workSpaceService.getWorkSpaceMetaData(workspaceId);
        return ResponseEntity.ok(ApiResponse.ok(toOutputDto(workspace)));
    }

    @GetMapping("/{workspaceId}/dashboard")
    public ResponseEntity<ApiResponse<WorkspaceDashboardDto>> getDashboard(@PathVariable UUID workspaceId) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(workSpaceService.getWorkspaceDashboard(workspaceId, userId)));
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<ApiResponse<List<WorkSpaceMemberInputDto>>> listMembers(
            @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.ok(workSpaceService.getMembersList(workspaceId)));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<String>> deleteWorkspace(@PathVariable UUID workspaceId) {
        workSpaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok(ApiResponse.ok("Workspace " + workspaceId + " deactivated"));
    }

    private static WorkSpaceOutputDto toOutputDto(WorkSpacesEntity workspace) {
        WorkSpaceOutputDto dto = new WorkSpaceOutputDto();
        dto.setWorkSpaceId(workspace.getWorkSpaceId());
        dto.setWorkSpaceName(workspace.getWorkSpaceName());
        return dto;
    }
}
