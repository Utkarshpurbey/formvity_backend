package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMemberEntity, UUID> {
    List<WorkspaceMemberEntity> findAllByUserId(UUID userId);
    List<WorkspaceMemberEntity> findByWorkspace_WorkSpaceId(UUID workSpaceId);

    boolean existsByWorkspace_WorkSpaceIdAndUserIdAndActiveTrue(UUID workSpaceId, UUID userId);
}
