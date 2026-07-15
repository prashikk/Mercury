package com.mercury.mercury.User;

import com.mercury.mercury.User.dto.CreateUserRequest;
import com.mercury.mercury.User.dto.UserResponse;
import com.mercury.mercury.User.entity.Role;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import com.mercury.mercury.User.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Test 1: User Created & Password Encrypted Successfully")
    void testUserCreatedSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john.doe");
        request.setEmail("john@mercury.com");
        request.setPassword("Password@123");
        request.setRole(Role.TRADER);

        UserEntity savedEntity = new UserEntity();
        savedEntity.setUserId(5L);
        savedEntity.setUsername("john.doe");
        savedEntity.setRole(Role.TRADER);

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@mercury.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(5L, result.getUserId());
        assertEquals("john.doe", result.getUsername());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(captor.capture());

        // Assert password encryption match (Task 7)
        assertTrue(encoder.matches("Password@123", captor.getValue().getPassword()));
    }

    @Test
    @DisplayName("Test 2: Duplicate Username Throws Error Check")
    void testDuplicateUsernameFails() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(new UserEntity()));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 3: Duplicate Email Throws Error Check")
    void testDuplicateEmailFails() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john.doe");
        request.setEmail("john@mercury.com");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@mercury.com")).thenReturn(Optional.of(new UserEntity()));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }
}

