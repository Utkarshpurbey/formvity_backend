package com.example.uttuCodes.formvity.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkSpaceRenameRequest {

    @NotBlank(message = "Workspace name is required")
    @JsonAlias({"workspaceName", "name"})
    private String workSpaceName;
}
