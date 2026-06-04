package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.FormInputDto;
import com.example.uttuCodes.formvity.dto.FormOutputDto;
import com.example.uttuCodes.formvity.dto.FormPatchDto;
import com.example.uttuCodes.formvity.enums.FormStatus;
import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.UserEntity;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.UserRepository;
import com.example.uttuCodes.formvity.repository.WorkSpaceRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberRepository;
import com.example.uttuCodes.formvity.service.FormService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    public List<FormOutputDto> getUserAllforms(UUID workspaceId, UUID userId) {
        //fetch only for active workspace and
        boolean isMember = workspaceMemberRepository
                .existsByWorkspace_WorkSpaceIdAndUserIdAndActiveTrue(workspaceId, userId);
        if (!isMember) {
            throw FormvityException.forbidden("You don't have access to this workspace");
        }
        return formRepository.findActiveByWorkspace(workspaceId);
    }

    @Override
    @Transactional
    public FormEntity createForm(FormInputDto input, UUID workspaceId, UUID userId) {
        WorkSpacesEntity workspace = workSpaceRepository.getReferenceById(workspaceId);
        UserEntity creator = userRepository.getReferenceById(userId);

        FormEntity form = new FormEntity();
        form.setTitle(input.getTitle().trim());
        form.setDraftPageDef(input.getDraftPageDef());
        form.setWorkspace(workspace);
        form.setCreatedByUser(creator);

        return formRepository.save(form);
    }

    @Override
    public Optional<Object> getFormDef(UUID workspaceId, UUID formId) {
        FormEntity formEntity = requireActiveFormInWorkspace(workspaceId, formId);
        return Optional.ofNullable(formEntity.getDraftPageDef());
    }

    @Override
    @Transactional
    public FormEntity patchForm(UUID workspaceId, UUID formId, FormPatchDto patch) {
        if (patch.getTitle() == null && patch.getDraftPageDef() == null) {
            throw FormvityException.badRequest("At least one of title or draftPageDef must be provided");
        }
        FormEntity form = requireActiveFormInWorkspace(workspaceId, formId);
        if (patch.getTitle() != null) {
            if (patch.getTitle().isBlank()) {
                throw FormvityException.badRequest("title cannot be blank");
            }
            form.setTitle(patch.getTitle().trim());
        }
        if (patch.getDraftPageDef() != null) {
            form.setDraftPageDef(patch.getDraftPageDef());
        }
        return formRepository.save(form);
    }

    @Override
    @Transactional
    public FormEntity replaceForm(UUID workspaceId, UUID formId, FormInputDto input) {
        FormEntity form = requireActiveFormInWorkspace(workspaceId, formId);
        form.setTitle(input.getTitle().trim());
        form.setDraftPageDef(input.getDraftPageDef());
        return formRepository.save(form);
    }

    @Override
    @Transactional
    public void deleteForm(UUID workspaceId, UUID formId, boolean hardDelete) {
        FormEntity form = requireActiveFormInWorkspace(workspaceId, formId);
        if (hardDelete) {
            formRepository.delete(form);
            return;
        }
        form.setStatus(FormStatus.ARCHIVED);
        formRepository.save(form);
    }

    private FormEntity requireActiveFormInWorkspace(UUID workspaceId, UUID formId) {
        FormEntity form = formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)
                .orElseThrow(() -> FormvityException.notFound(
                        "No form found with id " + formId + " in workspace " + workspaceId));
        if (form.getStatus() == FormStatus.ARCHIVED) {
            throw FormvityException.notFound(
                    "No form found with id " + formId + " in workspace " + workspaceId);
        }
        return form;
    }
}
