package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.PublishFormResponseDto;
import com.example.uttuCodes.formvity.dto.PublishStatusResponse;
import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.enums.FormStatus;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.service.FormPublicationService;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormPublicationServiceImpl implements FormPublicationService {

    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;
    private final WorkspaceAccessService workspaceAccessService;

    private String generateSlug(String formTitle,String publicId){
        String slugTitle = formTitle
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        return slugTitle + "-" + publicId;
    }

    @Override
    @Transactional
    public PublishFormResponseDto publishForm(UUID workspaceId, UUID formId, UUID userId) {
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
        FormEntity formDetails = formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)
                .orElseThrow(() -> FormvityException.notFound("No form exists with form id " + formId));
        if (formDetails.getDraftPageDef() == null) {
            throw FormvityException.badRequest("Missing page definition for this form");
        }

        FormPublicationEntity currentPublication =
                formPublicationRepository.findByForm_IdAndCurrentTrue(formId).orElse(null);
        int nextVersion = currentPublication == null ? 1 : currentPublication.getVersion() + 1;

        String publicId;
        String slug;
        if (currentPublication == null) {
            String alphabetWithoutSeparators = new String(NanoIdUtils.DEFAULT_ALPHABET)
                    .replace("-", "")
                    .replace("_", "");
            publicId = NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR, alphabetWithoutSeparators.toCharArray(), 10);
            slug = generateSlug(formDetails.getTitle(), publicId);
        } else {
            publicId = currentPublication.getPublicId();
            slug = currentPublication.getSlug();
            currentPublication.setCurrent(false);
            // Release the live slug so the new current publication row can reuse the public URL.
            currentPublication.setSlug(slug + "__v" + currentPublication.getVersion());
            formPublicationRepository.saveAndFlush(currentPublication);
        }

        FormPublicationEntity publication = new FormPublicationEntity();
        publication.setForm(formDetails);
        publication.setPublicId(publicId);
        publication.setSlug(slug);
        publication.setVersion(nextVersion);
        publication.setCurrent(true);
        publication.setPublishedPageDef(formDetails.getDraftPageDef());
        publication.setPublishedAt(LocalDateTime.now());

        FormPublicationEntity savedPublication = formPublicationRepository.save(publication);
        formDetails.setStatus(FormStatus.PUBLISHED);
        formRepository.save(formDetails);
        log.info("Published form {} version {} slug {}", formId, nextVersion, slug);
        return PublishFormResponseDto.from(savedPublication);
    }

    @Override
    public Map<String, Object> getPublishedPageDef(String slug) {

            String publicId = slug.substring(slug.lastIndexOf('-') + 1);
            FormPublicationEntity formPublicationEntity = formPublicationRepository.
                    findByPublicIdAndCurrentTrue(publicId).
                    orElseThrow(()->
                            new FormvityException(HttpStatus.NOT_FOUND, "No details found for the form"));
            return formPublicationEntity.getPublishedPageDef();
    }

    @Override
    public FormPublicationEntity unPublishForm(UUID workspaceId, UUID formId, UUID userId) {
            workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
            log.info("Form Unpublish request made in workspace {} and for form {} ", workspaceId, formId);
            FormEntity existingForm = formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)
                    .orElseThrow(() -> FormvityException.notFound("No form found. form Id " + formId));
            FormPublicationEntity existringPublishedForm = formPublicationRepository
                    .findByForm_IdAndCurrentTrue(formId)
                    .orElseThrow(() -> FormvityException.notFound("No active publication found for this form"));
            if(!existringPublishedForm.isCurrent()){
                log.warn("Unpublish attempted on already inactive form | formId = {}",formId);
                throw new FormvityException(HttpStatus.CONFLICT,"This form is already in inactive");
            }
            existringPublishedForm.setCurrent(false);
            existringPublishedForm.setUnpublishedAt(LocalDateTime.now());
            FormPublicationEntity saved = formPublicationRepository.save(existringPublishedForm);
            existingForm.setStatus(FormStatus.UNPUBLISHED);
            formRepository.save(existingForm);
            log.info("Form unpublished successfully | formId = {}", formId);
            return saved;
    }

    @Override
    public PublishStatusResponse getPublishStatus(UUID workspaceId, UUID formId, UUID userId) {
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
        FormEntity form = formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)
                .orElseThrow(() -> FormvityException.notFound("No form found with id " + formId));
        if (form.getStatus() == FormStatus.ARCHIVED) {
            throw FormvityException.notFound("No form found with id " + formId);
        }

        FormPublicationEntity current = formPublicationRepository
                .findByForm_IdAndCurrentTrue(formId)
                .orElse(null);
        FormPublicationEntity latest = formPublicationRepository
                .findTopByForm_IdOrderByVersionDesc(formId)
                .orElse(null);
        FormPublicationEntity ref = current != null ? current : latest;
        boolean isLive = current != null && form.getStatus() == FormStatus.PUBLISHED;

        return PublishStatusResponse.builder()
                .formId(formId)
                .formStatus(form.getStatus())
                .isLive(isLive)
                .slug(ref != null ? ref.getSlug() : null)
                .publicUrl(ref != null ? "/r/" + ref.getSlug() : null)
                .version(latest != null ? latest.getVersion() : null)
                .lastPublishedAt(latest != null ? latest.getPublishedAt() : null)
                .lastUnpublishedAt(
                        latest != null && !latest.isCurrent() ? latest.getUnpublishedAt() : null)
                .hasDraftChange(
                        ref != null && !Objects.equals(form.getDraftPageDef(), ref.getPublishedPageDef()))
                .build();
    }
}
