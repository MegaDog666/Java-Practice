package com.andreyk.practiceproject.core.service;

import com.andreyk.practiceproject.api.dto.auth.LoginResponse;

public interface KeycloakClient {
    LoginResponse authenticate(String username, String password);

    LoginResponse refreshToken(String refreshToken);
}
