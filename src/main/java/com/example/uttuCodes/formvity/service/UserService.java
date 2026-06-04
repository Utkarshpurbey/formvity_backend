package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.CurrentUser;
import com.example.uttuCodes.formvity.dto.UserDto;
import com.example.uttuCodes.formvity.dto.UserInputDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserInputDto addUser(UserInputDto u);

    CurrentUser getCurrentUser();
}
