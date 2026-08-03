package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.WorkSpaceMemberInputDto;
import com.example.uttuCodes.formvity.entity.UserEntity;
import com.example.uttuCodes.formvity.entity.WorkSpacesEntity;
import com.example.uttuCodes.formvity.entity.WorkspaceMemberEntity;
import com.example.uttuCodes.formvity.enums.FormRoles;
import com.example.uttuCodes.formvity.repository.UserRepository;
import com.example.uttuCodes.formvity.repository.WorkSpaceRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberInviteRepository;
import com.example.uttuCodes.formvity.repository.WorkspaceMemberRepository;
import com.example.uttuCodes.formvity.service.impl.WorkspaceMemberServiceImpl;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceImplTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkSpaceRepository workSpaceRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private WorkspaceMemberInviteRepository workspaceMemberInviteRepository;

    @InjectMocks
    private WorkspaceMemberServiceImpl workspaceMemberService;

    private UUID workspaceId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void getMembersList_ShouldPopulateUserNameAndDisplayName() {
        WorkSpacesEntity workspace = new WorkSpacesEntity();
        workspace.setWorkSpaceId(workspaceId);
        workspace.setWorkSpaceName("Test Workspace");

        WorkspaceMemberEntity member = WorkspaceMemberEntity.of(userId, workspace, FormRoles.ADMIN);

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setDisplayName("John Doe");
        user.setEmail("john@example.com");

        when(workspaceMemberRepository.findByWorkspace_WorkSpaceId(workspaceId)).thenReturn(List.of(member));
        when(userRepository.findAllById(List.of(userId))).thenReturn(List.of(user));

        List<WorkSpaceMemberInputDto> result = workspaceMemberService.getMembersList(workspaceId);

        assertNotNull(result);
        assertEquals(1, result.size());
        WorkSpaceMemberInputDto dto = result.get(0);
        assertEquals(userId, dto.getUserId());
        assertEquals("Test Workspace", dto.getWorkSpaceName());
        assertEquals(FormRoles.ADMIN, dto.getRole());
        assertEquals("John Doe", dto.getDisplayName());
        assertEquals("John Doe", dto.getUserName());
        assertEquals("john@example.com", dto.getEmail());
    }
}
