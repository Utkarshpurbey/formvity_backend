package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.TagsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagsRepository extends JpaRepository<TagsEntity, UUID> {

    List<TagsEntity> findByWorkSpace_WorkSpaceIdAndActiveTrue(UUID workspaceId);

    Optional<TagsEntity> findByIdAndActiveTrue(UUID id);

    Optional<TagsEntity> findByWorkSpace_WorkSpaceIdAndName(UUID workspaceId, String name);

    boolean existsByWorkSpace_WorkSpaceIdAndNameAndActiveTrue(UUID workspaceId, String name);

    boolean existsByWorkSpace_WorkSpaceIdAndNameAndActiveTrueAndIdNot(UUID workspaceId, String name, UUID id);
}
