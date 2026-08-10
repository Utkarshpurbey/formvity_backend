package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.TagSource;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagAttachRequestDto {

    @NotNull(message = "tagId is required")
    private UUID tagId;

    private TagSource source;
}
