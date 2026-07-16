package com.mercury.mercury.security;


import com.mercury.mercury.Security.dto.LoginRequest;
import com.mercury.mercury.Security.dto.LoginResponse;
import com.mercury.mercury.Security.service.AuthenticationService;
import com.mercury.mercury.Security.service.JwtService;
import com.mercury.mercury.User.entity.Role;
import com.mercury.mercury.User.entity.Status;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class SecurityAuthenticationTests {

    @Autowired private AuthenticationService authenticationService;
    @Autowired private JwtService jwtService;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setUserId(1L);
        mockUser.setUsername("john.doe");
        mockUser.setPassword(passwordEncoder.encode("Password@123"));
        mockUser.setRole(Role.TRADER);
        mockUser.setStatus(Status.ACTIVE);
    }

    @Test
    void testValidLogin() {
        when(userRepository.findByUsernameIgnoreCase("john.doe")).thenReturn(Optional.of(mockUser));
        LoginRequest req = new LoginRequest("john.doe", "Password@123");
        LoginResponse resp = authenticationService.authenticate(req);

        assertNotNull(resp.accessToken());
        assertEquals(900, resp.expiresIn());
    }

    @Test
    void testWrongPassword() {
        when(userRepository.findByUsernameIgnoreCase("john.doe")).thenReturn(Optional.of(mockUser));
        LoginRequest req = new LoginRequest("john.doe", "WrongPass!");

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(req));
    }

    @Test
    void testUnknownUsername() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());
        LoginRequest req = new LoginRequest("ghost", "Password@123");
        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(req));
    }

    @Test
    void testJwtWithValidationToken() {
        when(userRepository.findByUsernameIgnoreCase("john.doe")).thenReturn(Optional.of(mockUser));
        UserDetails details = userDetailsService.loadUserByUsername("john.doe");

        String token = jwtService.generateToken(details);
        assertNotNull(token);
        assertEquals("john.doe", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, details));
    }
}

