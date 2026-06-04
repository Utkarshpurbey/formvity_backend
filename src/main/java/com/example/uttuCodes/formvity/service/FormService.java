package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.FormInputDto;
import com.example.uttuCodes.formvity.dto.FormOutputDto;
import com.example.uttuCodes.formvity.dto.FormPatchDto;
import com.example.uttuCodes.formvity.entity.FormEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormService {
    List<FormOutputDto> getUserAllforms(UUID workspaceId, UUID userId);
    FormEntity createForm(FormInputDto input, UUID workspaceId, UUID userId);
    Optional<Object> getFormDef(UUID workspaceId, UUID formId);
    FormEntity patchForm(UUID workspaceId, UUID formId, FormPatchDto patch);
    FormEntity replaceForm(UUID workspaceId, UUID formId, FormInputDto input);
    void deleteForm(UUID workspaceId, UUID formId, boolean hardDelete);
}
