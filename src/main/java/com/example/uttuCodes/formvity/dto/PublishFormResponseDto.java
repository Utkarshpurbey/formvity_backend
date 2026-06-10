package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishFormResponseDto {
    private UUID formId;
    private String slug;
    private String publicUrl;
    private int version;
    private LocalDateTime publishedAt;

    public static PublishFormResponseDto from(FormPublicationEntity publication) {
        return PublishFormResponseDto.builder()
                .formId(publication.getForm().getId())
                .slug(publication.getSlug())
                .publicUrl("/f/" + publication.getSlug())
                .version(publication.getVersion())
                .publishedAt(publication.getPublishedAt())
                .build();
    }
}
