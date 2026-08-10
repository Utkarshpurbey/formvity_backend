package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.TagSource;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagBulkAttachRequestDto {

    @NotEmpty(message = "tagIds list cannot be empty")
    private List<UUID> tagIds;

    private TagSource source;
}
