package com.andreyk.practiceproject.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private int code;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}
