package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.FormSubmissionInputDto;
import com.example.uttuCodes.formvity.dto.SubmitFormResponseDto;
import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.SubmissionRepository;
import com.example.uttuCodes.formvity.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public SubmitFormResponseDto submitAnswer(FormSubmissionInputDto formSubmissionInputDto, String slug) {

        String publicId = slug.substring(slug.lastIndexOf('-') + 1);
        FormPublicationEntity formPublicationEntity = formPublicationRepository.
                findByPublicIdAndCurrentTrue(publicId)
                .orElseThrow(()-> new FormvityException(HttpStatus.BAD_REQUEST,String.format("Form doesn't exist or is not active ")));
        UUID formId = formPublicationEntity.getForm().getId();
        FormEntity formEntity =  formRepository
                .findById(formId)
                .orElseThrow(()-> new FormvityException(HttpStatus.BAD_REQUEST,String.format("Form doesn't exist for form | formId: ",formId) ));

       SubmissionEntity submissionEntity = new SubmissionEntity();
       submissionEntity.setAnswers(formSubmissionInputDto.getAnswers());
       submissionEntity.setForm(formEntity);
       submissionEntity.setMetadata(formSubmissionInputDto.getMetadata());
       submissionEntity.setPublication(formPublicationEntity);
       submissionEntity.setRespondent(formSubmissionInputDto.getRespondent());

       return SubmitFormResponseDto.from(submissionRepository.save(submissionEntity));
    }
}
