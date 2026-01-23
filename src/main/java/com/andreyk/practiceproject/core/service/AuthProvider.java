package com.andreyk.practiceproject.core.service;

import com.andreyk.practiceproject.api.dto.LoginResponse;

public interface AuthProvider {
    LoginResponse authenticate(String login, String password);
    LoginResponse refresh(String refreshToken);
}
