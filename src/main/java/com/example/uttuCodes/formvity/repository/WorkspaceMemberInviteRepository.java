package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.WorkspaceInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceMemberInviteRepository extends JpaRepository<WorkspaceInvitationEntity,String> {
    Optional<WorkspaceInvitationEntity> findByToken(String token);
}
