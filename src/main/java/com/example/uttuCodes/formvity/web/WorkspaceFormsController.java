package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.FormInputDto;
import com.example.uttuCodes.formvity.dto.FormOutputDto;
import com.example.uttuCodes.formvity.dto.FormPatchDto;
import com.example.uttuCodes.formvity.dto.PublishFormResponseDto;
import com.example.uttuCodes.formvity.dto.PublishStatusResponse;
import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.FormPublicationService;
import com.example.uttuCodes.formvity.service.FormService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * All form operations scoped under a workspace.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/workspaces/{workspaceId}/forms")
public class WorkspaceFormsController {

    private final FormService formService;
    private final FormPublicationService formPublicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FormOutputDto>>> listForms(@PathVariable UUID workspaceId) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(formService.getUserAllforms(workspaceId, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FormEntity>> createForm(
            @PathVariable UUID workspaceId,
            @RequestBody @Valid FormInputDto input) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(formService.createForm(input, workspaceId, userId)));
    }

    @GetMapping("/{formId}")
    public ResponseEntity<ApiResponse<?>> getFormDraft(@PathVariable UUID workspaceId, @PathVariable UUID formId) {
        return ResponseEntity.ok(ApiResponse.ok(formService.getFormDef(workspaceId, formId)));
    }

    @PatchMapping("/{formId}")
    public ResponseEntity<ApiResponse<FormEntity>> patchForm(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestBody @Valid FormPatchDto patch) {
        return ResponseEntity.ok(ApiResponse.ok(formService.patchForm(workspaceId, formId, patch)));
    }

    @PutMapping("/{formId}")
    public ResponseEntity<ApiResponse<FormEntity>> replaceForm(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestBody @Valid FormInputDto input) {
        return ResponseEntity.ok(ApiResponse.ok(formService.replaceForm(workspaceId, formId, input)));
    }

    @DeleteMapping("/{formId}")
    public ResponseEntity<ApiResponse<String>> deleteForm(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId,
            @RequestParam(defaultValue = "false") boolean hard) {
        formService.deleteForm(workspaceId, formId, hard);
        String message = hard
                ? "Form " + formId + " permanently deleted"
                : "Form " + formId + " archived";
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PostMapping("/{formId}/publish")
    public ResponseEntity<ApiResponse<PublishFormResponseDto>> publishForm(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId) {
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(formPublicationService.publishForm(workspaceId, formId, userId)));
    }

    @PostMapping("/{formId}/unpublish")
    public ResponseEntity<ApiResponse<?>> unpublishForm(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId
    ){
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(formPublicationService.unPublishForm(workspaceId,formId,userId)));
    }

    @GetMapping("/{formId}/publish-status")
    public ResponseEntity<ApiResponse<PublishStatusResponse>> publishStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID formId
    ){
        UUID userId = Utils.getLoggedInUserId();
        return ResponseEntity.ok(ApiResponse.ok(formPublicationService.getPublishStatus(workspaceId,formId,userId)));
    }
}
