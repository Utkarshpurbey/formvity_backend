package com.example.uttuCodes.formvity.entity;

import com.example.uttuCodes.formvity.enums.FormRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@Table(name = "workspace_members")
public class WorkspaceMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @NonNull
    public UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    public WorkSpacesEntity workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormRoles role;

    @Column(nullable = false)
    private Boolean active;

    @NotNull
    public LocalDateTime createdAt;


    @PrePersist
    void prepersist(){
        if(this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
        if (this.role == null) {
            this.role = FormRoles.ATTENDEE;
        }
        if(this.active == null){
            this.active = Boolean.TRUE;
        }
    }
}
