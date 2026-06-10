package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.PublishFormResponseDto;
import com.example.uttuCodes.formvity.dto.PublishStatusResponse;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;

import java.util.Map;
import java.util.UUID;

public interface FormPublicationService {
    PublishFormResponseDto publishForm(UUID workspaceId, UUID formId, UUID userId);
    Map<String ,Object> getPublishedPageDef(String slug);
    FormPublicationEntity unPublishForm(UUID workspaceId,UUID formId,UUID userId);
    PublishStatusResponse getPublishStatus (UUID workspaceId, UUID formId, UUID userId);
}
