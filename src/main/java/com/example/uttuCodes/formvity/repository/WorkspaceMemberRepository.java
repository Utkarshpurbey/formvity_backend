package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMemberEntity, UUID> {
    List<WorkspaceMemberEntity> findAllByUserId(UUID userId);
    List<WorkspaceMemberEntity> findByWorkspace_WorkSpaceId(UUID workSpaceId);
    Optional<WorkspaceMemberEntity> findByUserIdAndWorkspace_WorkSpaceId(UUID userId, UUID workspaceId);

    boolean existsByWorkspace_WorkSpaceIdAndUserIdAndActiveTrue(UUID workSpaceId, UUID userId);
}
