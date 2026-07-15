package com.mercury.mercury.User.dto;

import com.mercury.mercury.User.entity.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long userId;
    private String username;
    private Role role;
}
