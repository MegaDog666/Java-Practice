package com.andreyk.practiceproject.core.service.impl;

import com.andreyk.practiceproject.api.dto.auth.LoginRequest;
import com.andreyk.practiceproject.api.dto.auth.LoginResponse;
import com.andreyk.practiceproject.api.dto.auth.RefreshRequest;
import com.andreyk.practiceproject.core.service.AuthProvider;
import com.andreyk.practiceproject.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthProvider authProvider;

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        return authProvider.authenticate(
                request.getUsername(),
                request.getPassword()
        );
    }

    @Override
    public LoginResponse refreshToken(RefreshRequest request) {
        return authProvider.refresh(request.getRefreshToken());
    }
}
