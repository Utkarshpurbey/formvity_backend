package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.dto.FormOutputDto;
import com.example.uttuCodes.formvity.enums.FormStatus;
import com.example.uttuCodes.formvity.entity.FormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormRepository extends JpaRepository<FormEntity, UUID> {

    Optional<FormEntity> findByIdAndWorkspace_WorkSpaceId(UUID id, UUID workspaceId);

    /**
     * Forms in the given active workspace, newest {@code updatedAt} first.
     * Authorization (membership) is handled in the service layer.
     */
    @Query("""
            SELECT new com.example.uttuCodes.formvity.dto.FormOutputDto(
                f.id,
                f.workspace.workSpaceId,
                f.createdByUser.id,
                f.status,
                f.title,
                f.createdAt,
                f.updatedAt
            )
            FROM FormEntity f
            WHERE f.workspace.workSpaceId = :workspaceId
              AND f.status <> com.example.uttuCodes.formvity.enums.FormStatus.ARCHIVED
            ORDER BY f.updatedAt DESC
            """)
    List<FormOutputDto> findActiveByWorkspace(@Param("workspaceId") UUID workspaceId);

    long countByWorkspace_WorkSpaceIdAndStatusNot(UUID workspaceId, FormStatus status);
}
