package com.example.uttuCodes.formvity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "form_publications",
        indexes = {
                @Index(name = "idx_form_publications_form_current", columnList = "form_id,is_current"),
                @Index(name = "idx_form_publications_form_version", columnList = "form_id,version")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_form_publications_slug", columnNames = "slug"))
public class FormPublicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    private FormEntity form;

    @NotNull
    private String publicId;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String slug;

    @Column(nullable = false)
    private int version;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> publishedPageDef;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    private LocalDateTime unpublishedAt;

    @PrePersist
    void prePersist() {
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
        if (version == 0) {
            this.version = 0;
        }
    }
}
