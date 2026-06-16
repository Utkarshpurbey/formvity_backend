package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.ActivateUserDto;
import com.example.uttuCodes.formvity.dto.ActivateUserResponseDto;
import com.example.uttuCodes.formvity.dto.InvitePreviewDto;
import com.example.uttuCodes.formvity.entity.UserEntity;
import com.example.uttuCodes.formvity.entity.WorkspaceInvitationEntity;
import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;
import com.example.uttuCodes.formvity.enums.InviteStatus;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.UserRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberInviteRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberRepository;
import com.example.uttuCodes.formvity.security.JwtService;
import com.example.uttuCodes.formvity.service.UserActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivationServiceImpl implements UserActivationService {

    private static final Set<InviteStatus> ACTIVATABLE_STATUSES = Set.of(InviteStatus.INVITED, InviteStatus.PENDING);

    private final WorkspaceMemberInviteRepository workspaceMemberInviteRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public InvitePreviewDto getTokenDetails(String token) {
        WorkspaceInvitationEntity invitation = requireInvitation(token, false);

        return InvitePreviewDto.builder()
                .email(invitation.getEmail())
                .workspaceId(invitation.getWorkSpaces().getWorkSpaceId())
                .workspaceName(invitation.getWorkSpaces().getWorkSpaceName())
                .role(invitation.getFormRoles())
                .expiresAt(invitation.getExpireAt())
                .build();
    }

    @Override
    @Transactional
    public ActivateUserResponseDto activateUser(ActivateUserDto request) {
        WorkspaceInvitationEntity invitation = requireInvitation(request.getToken(), true);

        String email = invitation.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw FormvityException.conflict("An account with this email already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setDisplayName(request.getDisplayName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        WorkspaceMemberEntity member = workspaceMemberRepository.save(
                WorkspaceMemberEntity.of(user.getId(), invitation.getWorkSpaces(), invitation.getFormRoles()));

        invitation.setStatus(InviteStatus.ACCEPTED);
        workspaceMemberInviteRepository.save(invitation);

        String jwt = jwtService.generateToken(user.getId(), user.getDisplayName());
        log.info("Activated invited user id={} workspaceId={}", user.getId(), invitation.getWorkSpaces().getWorkSpaceId());

        return ActivateUserResponseDto.builder()
                .token(jwt)
                .id(user.getId())
                .displayName(user.getDisplayName())
                .workspaceId(invitation.getWorkSpaces().getWorkSpaceId())
                .joinedAt(member.getCreatedAt())
                .build();
    }

    private WorkspaceInvitationEntity requireInvitation(String token, boolean markExpired) {
        WorkspaceInvitationEntity invitation = workspaceMemberInviteRepository.findByToken(token)
                .orElseThrow(() -> FormvityException.notFound("No invitation found for token"));

        if (invitation.getStatus() == InviteStatus.ACCEPTED) {
            throw FormvityException.conflict("This invitation has already been used");
        }
        if (invitation.getStatus() == InviteStatus.EXPIRED) {
            throw new FormvityException(HttpStatus.GONE, "This invitation has expired");
        }
        if (!ACTIVATABLE_STATUSES.contains(invitation.getStatus())) {
            throw FormvityException.badRequest("This invitation is no longer valid");
        }
        if (invitation.getExpireAt() != null && invitation.getExpireAt().isBefore(LocalDateTime.now())) {
            if (markExpired) {
                invitation.setStatus(InviteStatus.EXPIRED);
                workspaceMemberInviteRepository.save(invitation);
            }
            throw new FormvityException(HttpStatus.GONE, "This invitation has expired");
        }

        return invitation;
    }
}
