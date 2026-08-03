package com.example.uttuCodes.formvity.service.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.example.uttuCodes.formvity.dto.InviteUserDto;
import com.example.uttuCodes.formvity.dto.WorkSpaceMemberInputDto;
import com.example.uttuCodes.formvity.dto.response.InvitationSentResponseDto;
import com.example.uttuCodes.formvity.entity.UserEntity;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.entity.WorkspaceInvitationEntity;
import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;
import com.example.uttuCodes.formvity.enums.InviteStatus;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.UserRepository;
import com.example.uttuCodes.formvity.repository.WorkSpaceRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberInviteRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberRepository;
import com.example.uttuCodes.formvity.service.WorkspaceMemberService;
import com.example.uttuCodes.formvity.utils.Utils;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final UserRepository userRepository;
    private final WorkSpaceRepository workSpaceRepository;
    private final ModelMapper modelMapper;
    private final WorkspaceMemberInviteRepository workspaceMemberInviteRepository;

    @Override
    public List<WorkSpaceMemberInputDto> getMembersList(UUID workSpaceId) {
        try {
            List<WorkspaceMemberEntity> w = workspaceMemberRepository.findByWorkspace_WorkSpaceId(workSpaceId);
            List<UUID> userIds = w.stream()
                    .map(WorkspaceMemberEntity::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            Map<UUID, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (a, b) -> a));

            return w.stream()
                    .map(u -> {
                        WorkSpaceMemberInputDto dto = modelMapper.map(u, WorkSpaceMemberInputDto.class);
                        if (u.getUserId() != null && userMap.containsKey(u.getUserId())) {
                            UserEntity user = userMap.get(u.getUserId());
                            dto.setDisplayName(user.getDisplayName());
                            dto.setUserName(user.getDisplayName());
                            dto.setEmail(user.getEmail());
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Unable to load workspace members for workspace {}", workSpaceId, e);
            throw FormvityException.internalServerError(
                    "Unable to load workspace members for workspace " + workSpaceId + ". Please try again later.");
        }
    }


    private String buildInviteUrl(String token, String email) {
        if (token != null && !token.isEmpty()) {
            return String.format("%s/welcome?token=%s&email=%s", frontendUrl, token, email);
        }
        // For existing users, just return the workspace URL
        return String.format("%s/workspace", frontendUrl);
    }

    @Override
    public InvitationSentResponseDto inviteUser(UUID workspaceId, InviteUserDto inviteUserDto) {
        UUID currentUserId = Utils.getLoggedInUserId();
        // Check if the logged in user in that workspace
        workspaceAccessService.requireUserExistInWorkSpace(currentUserId, workspaceId);
        log.info(String.format("Invite request initiated | emailId %s and workspaceId %s",inviteUserDto.getEmail(),workspaceId));
        String token = null;
        UserEntity userEntity = userRepository.findByEmail(inviteUserDto.getEmail()).orElse(null);
        boolean isExistingUser = userEntity != null;
        WorkSpacesEntity workSpacesEntity = workSpaceRepository.findByWorkSpaceId(workspaceId);

        if(workSpacesEntity == null){
            log.error(String.format("Invite request failed | workspace not found | workspaceId %s" ,workspaceId));
            throw new FormvityException(HttpStatus.BAD_REQUEST,String.format("No workspace found with | workSpaceId ", workspaceId));
        }

       if(isExistingUser){
           log.info(String.format("Invite request for existing user | userId %s",userEntity.getId()));
           boolean isExistingMember = workspaceAccessService.isPartOfWorkspace(userEntity.getId(), workspaceId);
           if(isExistingMember){
               throw new FormvityException(HttpStatus.CONFLICT,String.format("This user already part of this workspace | UserId - %s",userEntity.getId()));
           }
           log.info(String.format("Invite request successful! | userId %s",userEntity.getId()));
           workspaceMemberRepository.save(WorkspaceMemberEntity.of(userEntity.getId(),workSpacesEntity,inviteUserDto.getRole()));
       } else {
           log.info("Invite request for a new user | email {}", inviteUserDto.getEmail());
           String requiredAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
           token = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, requiredAlphabet.toCharArray(), 30);
           WorkspaceInvitationEntity workspaceInvitationEntity = WorkspaceInvitationEntity.builder()
                   .invitedByUserId(currentUserId)
                   .invitedAt(LocalDateTime.now())
                   .status(InviteStatus.INVITED)
                   .token(token)
                   .email(inviteUserDto.getEmail())
                   .formRoles(inviteUserDto.getRole())
                   .expireAt(LocalDateTime.now().plusDays(7))
                   .workSpaces(workSpacesEntity)
                   .build();
           log.info("Invite request for a new user | email {} token {}", inviteUserDto.getEmail(), token);
           workspaceMemberInviteRepository.save(workspaceInvitationEntity);
       }

       return InvitationSentResponseDto.builder().userId(isExistingUser? userEntity.getId():null)
               .isNewUser(!isExistingUser)
               .roles(inviteUserDto.getRole())
               .emailId(inviteUserDto.getEmail())
               .joinedAt(LocalDateTime.now())
               .workspaceId(workspaceId)
               .inviteUrl(buildInviteUrl(token,inviteUserDto.getEmail()))
               .build();
    }
}
