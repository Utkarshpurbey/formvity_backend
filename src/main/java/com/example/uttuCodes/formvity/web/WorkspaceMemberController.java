package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.InviteUserDto;
import com.example.uttuCodes.formvity.dto.WorkSpaceMemberInputDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.WorkspaceMemberService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/workspaces/{workspaceId}/members")
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkSpaceMemberInputDto>>> listMembers(
            @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceMemberService.getMembersList(workspaceId)));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<?>> inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteUserDto inviteUserDto) {
        return ResponseEntity.ok(ApiResponse.ok(workspaceMemberService.inviteUser(workspaceId, inviteUserDto)));
    }
}
