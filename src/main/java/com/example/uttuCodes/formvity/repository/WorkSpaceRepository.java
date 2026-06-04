package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkSpaceRepository extends JpaRepository<WorkSpacesEntity, UUID> {
    WorkSpacesEntity findByWorkSpaceId(UUID workSpaceId);
}
