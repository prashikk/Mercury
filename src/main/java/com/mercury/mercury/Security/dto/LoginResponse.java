package com.mercury.mercury.Security.dto;

public record LoginResponse(String accessToken, long expiresIn) {}