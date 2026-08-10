package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {

    long countByForm_Id(UUID formId);

    long countByForm_IdAndCreatedAtAfter(UUID formId, LocalDateTime after);

    Optional<SubmissionEntity> findFirstByForm_IdOrderByCreatedAtAsc(UUID formId);

    Optional<SubmissionEntity> findFirstByForm_IdOrderByCreatedAtDesc(UUID formId);

    Page<SubmissionEntity> findByForm_IdOrderByCreatedAtDesc(UUID formId, Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM SubmissionEntity s
            JOIN SubmissionTagEntity st ON st.submission.id = s.id
            WHERE s.form.id = :formId
              AND st.tag.id = :tagId
              AND st.tag.active = true
            ORDER BY s.createdAt DESC
            """)
    Page<SubmissionEntity> findByFormIdAndTagIdOrderByCreatedAtDesc(@Param("formId") UUID formId, @Param("tagId") UUID tagId, Pageable pageable);

    @Query("""
            SELECT s FROM SubmissionEntity s
            JOIN FETCH s.publication
            WHERE s.form.id = :formId
            ORDER BY s.createdAt DESC
            """)
    List<SubmissionEntity> findAllWithPublicationByFormId(@Param("formId") UUID formId);

    @Query(value = """
            SELECT CAST(created_at AS date) AS date, COUNT(*) AS count
            FROM submissions
            WHERE form_id = :formId
              AND created_at >= :from
            GROUP BY CAST(created_at AS date)
            ORDER BY date
            """, nativeQuery = true)
    List<Object[]> countDailyByFormIdSince(@Param("formId") UUID formId, @Param("from") LocalDateTime from);
}
