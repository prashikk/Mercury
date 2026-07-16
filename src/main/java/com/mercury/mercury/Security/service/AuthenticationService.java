package com.mercury.mercury.Security.service;

import com.mercury.mercury.Security.dto.LoginRequest;
import com.mercury.mercury.Security.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 UserDetailsService userDetailsService,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Login Attempt for user: {}", request.username());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            log.info("Password Verified");
        } catch (BadCredentialsException e) {
            log.error("Authentication Failed: Wrong Password for user: {}", request.username());
            throw e;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("JWT Generated");
        log.info("Authentication Successful");
        return new LoginResponse(jwtToken, jwtService.getExpirationTimeSeconds());
    }
}
