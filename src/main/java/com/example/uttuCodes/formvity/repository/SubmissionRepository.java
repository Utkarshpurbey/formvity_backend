package com.example.uttuCodes.formvity.repository;

import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {}
