package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.config.MyUserDetailsService;
import com.example.uttuCodes.formvity.dto.CurrentUser;
import com.example.uttuCodes.formvity.dto.UserDto;
import com.example.uttuCodes.formvity.dto.UserInputDto;
import com.example.uttuCodes.formvity.entity.UserEntity;
import com.example.uttuCodes.formvity.repository.UserRepository;
import com.example.uttuCodes.formvity.service.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final MyUserDetailsService userDetailsService;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.fetchUserDto();
    }

    @Override
    public UserInputDto addUser(UserInputDto u) {
        UserEntity userEntity = modelMapper.map(u, UserEntity.class);
        userEntity.setEmail(u.getEmail().trim().toLowerCase(Locale.ROOT));
        userEntity.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(userEntity);
        return u;
    }


    @Override
    public CurrentUser getCurrentUser() {
        return userDetailsService.getCurrentUser();
    }
}