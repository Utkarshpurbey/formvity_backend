package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.ActivateUserDto;
import com.example.uttuCodes.formvity.dto.ActivateUserResponseDto;
import com.example.uttuCodes.formvity.dto.InvitePreviewDto;

public interface UserActivationService {

    InvitePreviewDto getTokenDetails(String token);

    ActivateUserResponseDto activateUser(ActivateUserDto request);
}
