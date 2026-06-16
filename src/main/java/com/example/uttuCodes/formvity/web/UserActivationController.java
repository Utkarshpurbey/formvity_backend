package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.ActivateUserDto;
import com.example.uttuCodes.formvity.dto.ActivateUserResponseDto;
import com.example.uttuCodes.formvity.dto.InvitePreviewDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.service.UserActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserActivationController {

    private final UserActivationService userActivationService;

    @GetMapping("/invite/{token}")
    public ResponseEntity<ApiResponse<InvitePreviewDto>> getTokenDetails(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(userActivationService.getTokenDetails(token)));
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<ActivateUserResponseDto>> activateUser(
            @Valid @RequestBody ActivateUserDto request) {
        ActivateUserResponseDto response = userActivationService.activateUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Account activated"));
    }
}
