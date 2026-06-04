package com.example.uttuCodes.formvity.config;

import com.example.uttuCodes.formvity.dto.CurrentUser;
import com.example.uttuCodes.formvity.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String lookup = username == null ? null : username.trim();
        return userRepository
                .findByLoginIdentifier(lookup)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getDisplayName())
                        .password(user.getPassword())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + lookup));
    }

    public CurrentUser getCurrentUser(){
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

       return userRepository.findByDisplayName(username).map(user -> new CurrentUser(user.getId(),user.getDisplayName()))
                .orElseThrow(()-> new UsernameNotFoundException("No user loged in"));
    }
}
