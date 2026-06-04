package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.enums.FormStatus;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.service.FormPublicationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormPublicationServiceImpl implements FormPublicationService {

    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;

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
    public Optional<FormPublicationEntity> publishForm(UUID workspaceId, UUID formId, UUID userId) {
        try {
            FormEntity formDetails = formRepository.findById(formId).orElseThrow(() -> new FormvityException(HttpStatus.NOT_FOUND, "No form exists with form id" + formId));
            if(formDetails.getDraftPageDef() == null){
                throw new FormvityException(HttpStatus.BAD_REQUEST,"Missing page Def for this form. Please try another form ");
            }
            FormPublicationEntity currentPublication = formPublicationRepository.findByFormIdAndCurrentTrue(formId).orElse(null);
            int nextVersion = currentPublication == null ? 1: currentPublication.getVersion() +1;
            String publicId,slug;
            if(currentPublication == null){
                 publicId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,NanoIdUtils.DEFAULT_ALPHABET,10);
                 slug = generateSlug(formDetails.getTitle(),publicId);
            }else{
                publicId = currentPublication.getPublicId();
                slug = currentPublication.getSlug();
                currentPublication.setCurrent(false);
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
            return Optional.of(savedPublication);
        } catch (Exception e) {
            throw new FormvityException(HttpStatus.BAD_REQUEST, "Error is publishing the form");
        }
    }

    @Override
    public Map<String, Object> getPublishedPageDef(String slug) {
        try {
            String publicId = slug.substring(slug.lastIndexOf('-') + 1);
            FormPublicationEntity formPublicationEntity = formPublicationRepository.
                    findByPublicIdAndCurrentTrue(publicId).
                    orElseThrow(()->
                            new FormvityException(HttpStatus.NOT_FOUND, "No details found for the form"));
            return formPublicationEntity.getPublishedPageDef();
        }catch (Exception e){
            throw new FormvityException(HttpStatus.BAD_REQUEST, "Error is fetching the form details");
        }
    }
}
