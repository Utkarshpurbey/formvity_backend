package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.entity.SubmissionTagEntity;
import com.example.uttuCodes.formvity.entity.TagsEntity;
import com.example.uttuCodes.formvity.enums.TagSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagResponseDto {

    private UUID id;
    private String name;
    private String hexCode;
    private TagSource source;

    public static TagResponseDto fromEntity(TagsEntity entity) {
        if (entity == null) {
            return null;
        }
        return TagResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .hexCode(entity.getHexCode())
                .build();
    }

    public static TagResponseDto fromSubmissionTag(SubmissionTagEntity submissionTag) {
        if (submissionTag == null || submissionTag.getTag() == null) {
            return null;
        }
        TagsEntity tag = submissionTag.getTag();
        return TagResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .hexCode(tag.getHexCode())
                .source(submissionTag.getSource() != null ? submissionTag.getSource() : TagSource.MANUAL)
                .build();
    }
}
