package com.example.uttuCodes.formvity.entity;

import com.example.uttuCodes.formvity.enums.FormRoles;
import com.example.uttuCodes.formvity.enums.InviteStatus;
import jakarta.persistence.*;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInvitationEntity {
    @NotBlank
    @Id
    @Column(unique = true)
    private String token;
    @Email
    private String email;
    @NotNull
    private FormRoles formRoles;

    @NotNull
    private UUID invitedByUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkSpacesEntity workSpaces;

    @NotNull
    private InviteStatus status;

    private LocalDateTime invitedAt;
    private LocalDateTime expireAt;

//    @PrePersist
//    void prePersist(){
//    }

}
