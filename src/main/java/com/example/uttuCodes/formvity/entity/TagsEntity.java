package com.example.uttuCodes.formvity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = "tags", uniqueConstraints = {@UniqueConstraint(name = "formvityTagConstraint",columnNames = {"workspace_id","name"})})
public class TagsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkSpacesEntity workSpace;

    @NotBlank
    @Column(nullable = false,length = 30)
    private String name;

    @NotBlank
    @Column(nullable = false,length = 7)
    private String hexCode;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @PrePersist
    void presist(){
        if(this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
        if(this.updatedAt == null){
            this.updatedAt = LocalDateTime.now();
        }
        if(this.hexCode == null || this.hexCode.isBlank()){
            this.hexCode = "#3b82f6";
        }
    }

    @PreUpdate
    void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
