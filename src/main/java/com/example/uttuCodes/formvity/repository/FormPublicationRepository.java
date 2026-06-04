package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FormPublicationRepository extends JpaRepository<FormPublicationEntity, UUID> {
    Optional<FormPublicationEntity> findByFormIdAndCurrentTrue(UUID formid);
    Optional<FormPublicationEntity> findByPublicIdAndCurrentTrue(String slug);
}
