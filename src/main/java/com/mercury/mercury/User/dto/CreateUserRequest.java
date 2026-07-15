package com.mercury.mercury.User.dto;

import com.mercury.mercury.User.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotNull(message = "Username cannot be null")
    @Size(min = 5, message = "Username must be minimum 5 characters")
    private String username;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Please supply a valid Email format pattern")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain uppercase, lowercase, number, and special character details with at least 8 elements total"
    )
    private String password;

    @NotNull(message = "Role cannot be null")
    private Role role;
}
