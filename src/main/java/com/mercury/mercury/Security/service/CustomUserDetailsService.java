package com.mercury.mercury.Security.service;

import com.mercury.mercury.Security.domain.CustomUserDetails;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("User Found: Checking database registry records for user: {}", username);
        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> {
                    log.error("Authentication Failed: User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
