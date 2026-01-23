package com.andreyk.practiceproject.core.service;

import com.andreyk.practiceproject.api.dto.LoginRequest;
import com.andreyk.practiceproject.api.dto.LoginResponse;
import com.andreyk.practiceproject.api.dto.RefreshRequest;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);

    LoginResponse refreshToken(RefreshRequest request);
}
