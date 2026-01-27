package com.andreyk.practiceproject.core.service;

import com.andreyk.practiceproject.api.dto.auth.LoginResponse;

public interface AuthProvider {
    LoginResponse authenticate(String login, String password);
    LoginResponse refresh(String refreshToken);
}
