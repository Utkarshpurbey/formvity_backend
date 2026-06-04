package com.example.uttuCodes.formvity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank
    private String displayName;
    @NotBlank
    @Size(min = 8)
    @Column(length = 255)
    private String password;
    @Email
    @NotNull
    @Column(unique = true)
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prepersist(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
        if(updatedAt == null){
            updatedAt = LocalDateTime.now();
        }
    }
}
