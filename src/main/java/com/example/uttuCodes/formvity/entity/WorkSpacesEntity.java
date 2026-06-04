package com.example.uttuCodes.formvity.entity;

import com.example.uttuCodes.formvity.enums.FormRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workspaces")
public class WorkSpacesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID workSpaceId;

    @NotNull
    private String workSpaceName;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;

    @NotNull
    private UUID createdByUser;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @PrePersist
    void prepersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}
