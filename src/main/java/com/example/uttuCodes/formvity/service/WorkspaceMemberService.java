package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.InviteUserDto;
import com.example.uttuCodes.formvity.dto.WorkSpaceMemberInputDto;
import com.example.uttuCodes.formvity.dto.response.InvitationSentResponseDto;
import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;

import java.util.List;
import java.util.UUID;

public interface WorkspaceMemberService {

    List<WorkSpaceMemberInputDto> getMembersList(UUID workSpaceId);
    InvitationSentResponseDto inviteUser(UUID worksaceId, InviteUserDto inviteUserDto);
}
