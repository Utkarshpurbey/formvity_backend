package com.example.uttuCodes.formvity.utils;

import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class WorkspaceAccessService {
    private WorkspaceMemberRepository workspaceMemberRepository;

    public boolean isPartOfWorkspace(UUID userId,UUID workspaceId){
        return workspaceMemberRepository.existsByWorkspace_WorkSpaceIdAndUserIdAndActiveTrue(workspaceId,userId);
    }

    public void requireUserExistInWorkSpace(UUID userId, UUID workspaceId){
        boolean isMember = isPartOfWorkspace(userId,workspaceId);
         if(!isMember){
             throw FormvityException.forbidden(
                     "User is either not active or is not part of this workspace " + workspaceId);
         }
    }
}
