package com.andreyk.practiceproject.service;

import com.andreyk.practiceproject.dto.LoginRequest;
import com.andreyk.practiceproject.dto.LoginResponse;
import com.andreyk.practiceproject.dto.RefreshRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakClientService keycloakClient;


    public LoginResponse login(LoginRequest request) {
        return keycloakClient.authenticate(
                request.login(),
                request.password()
        );
    }

    public LoginResponse refreshToken(RefreshRequest request) {
        return keycloakClient.refreshToken(request.refreshToken());
    }
}
