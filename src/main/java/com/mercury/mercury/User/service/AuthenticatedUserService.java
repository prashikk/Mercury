package com.mercury.mercury.User.service;

import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> {
                    log.error("Identity propagation failed: username '{}' not found in registry records.", username);
                    return new RuntimeException("Authenticated user not found in database registry: " + username);
                });
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Access Denied: Unauthenticated access attempt intercepted.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
