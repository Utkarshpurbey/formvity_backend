package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class FormAccessService {
    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;
    public boolean isFormPartOfWorkspace(UUID formId, UUID workspaceId){
        FormEntity formEntity =  formRepository.findById(formId).orElseThrow(()-> new FormvityException(HttpStatus.BAD_REQUEST,String.format("Form doesn't exist for form | formId: ",formId) ));
        return formEntity.getWorkspace().getWorkSpaceId() == workspaceId;
    }

    public boolean isFormPublished(UUID formId){
        FormPublicationEntity formPublicationEntity = formPublicationRepository.
                findByForm_IdAndCurrentTrue(formId)
                .orElseThrow(()-> new FormvityException(HttpStatus.BAD_REQUEST,String.format("Form doesn't exist or is not active for form | formId: ",formId)));
        return true;
    }
}
