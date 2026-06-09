package com.example.uttuCodes.formvity.utils;

import com.example.uttuCodes.formvity.exception.FormvityException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Reads the authenticated user id set by {@link com.example.uttuCodes.formvity.security.JwtAuthFilter}.
 */
public final class Utils {

    private Utils() {
    }

    public static UUID getLoggedInUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw FormvityException.unauthorized("Please re-login");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        throw FormvityException.unauthorized("Please re-login");
    }
}
