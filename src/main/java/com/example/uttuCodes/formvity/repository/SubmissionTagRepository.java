package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.SubmissionTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionTagRepository extends JpaRepository<SubmissionTagEntity, UUID> {

    Optional<SubmissionTagEntity> findBySubmission_IdAndTag_Id(UUID submissionId, UUID tagId);

    List<SubmissionTagEntity> findBySubmission_Id(UUID submissionId);

    List<SubmissionTagEntity> findBySubmission_Form_Id(UUID formId);

    List<SubmissionTagEntity> findBySubmission_IdIn(List<UUID> submissionIds);

    boolean existsBySubmission_IdAndTag_Id(UUID submissionId, UUID tagId);

    void deleteBySubmission_IdAndTag_Id(UUID submissionId, UUID tagId);
}
