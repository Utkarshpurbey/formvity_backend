package com.example.uttuCodes.formvity.web;

import com.example.uttuCodes.formvity.dto.CurrentUser;
import com.example.uttuCodes.formvity.dto.LoginDto;
import com.example.uttuCodes.formvity.dto.LoginResponse;
import com.example.uttuCodes.formvity.dto.UserInputDto;
import com.example.uttuCodes.formvity.dto.response.ApiResponse;
import com.example.uttuCodes.formvity.entity.UserEntity;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.UserRepository;
import com.example.uttuCodes.formvity.security.JwtService;
import com.example.uttuCodes.formvity.utils.Utils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginDto request) {
        UserEntity user = userRepository
                .findByLoginIdentifier(request.getUserName())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> FormvityException.unauthorized("Invalid username or password"));

        String token = jwtService.generateToken(user.getId(), user.getDisplayName());
        log.info("User logged in id={}", user.getId());
        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getDisplayName()));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUser> me() {
        UUID userId = Utils.getLoggedInUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> FormvityException.unauthorized("User not found"));
        return ResponseEntity.ok(new CurrentUser(user.getId(), user.getDisplayName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully. Discard the token on the client."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> registerUser(@RequestBody @Valid UserInputDto u) {
        UserEntity user = modelMapper.map(u, UserEntity.class);
        user.setEmail(u.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(user);
        log.info("Registered new user id={} email={}", user.getId(), user.getEmail());

        String token = jwtService.generateToken(user.getId(), user.getDisplayName());
        LoginResponse loginResponse = new LoginResponse(token, user.getId(), user.getDisplayName());
        return ResponseEntity.ok(ApiResponse.created(loginResponse, "User added successfully"));
    }
}
