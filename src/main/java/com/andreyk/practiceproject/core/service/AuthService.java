package com.andreyk.practiceproject.core.service;

import com.andreyk.practiceproject.api.dto.auth.LoginRequest;
import com.andreyk.practiceproject.api.dto.auth.LoginResponse;
import com.andreyk.practiceproject.api.dto.auth.RefreshRequest;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);

    LoginResponse refreshToken(RefreshRequest request);
}
