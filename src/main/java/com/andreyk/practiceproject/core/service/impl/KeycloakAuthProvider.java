package com.andreyk.practiceproject.core.service.impl;

import com.andreyk.practiceproject.api.dto.LoginResponse;
import com.andreyk.practiceproject.core.service.AuthProvider;
import com.andreyk.practiceproject.core.service.KeycloakClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KeycloakAuthProvider implements AuthProvider {
    private final KeycloakClient keycloakClient;

    @Override
    public LoginResponse authenticate(String login, String password) {
        return keycloakClient.authenticate(login, password);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        return keycloakClient.refreshToken(refreshToken);
    }
}
