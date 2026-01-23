package com.andreyk.practiceproject.service;

import com.andreyk.practiceproject.dto.KeycloakTokenResponse;
import com.andreyk.practiceproject.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KeycloakClientService {

    private final RestTemplate restTemplate;

    @Value("${KEYCLOAK_URL}")
    private String url;

    @Value("${KEYCLOAK_CLIENT_ID}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;


    public LoginResponse authenticate(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        KeycloakTokenResponse token = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                KeycloakTokenResponse.class
        ).getBody();

        return mapToLoginResponse(token);
    }

    public LoginResponse refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        KeycloakTokenResponse token = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                KeycloakTokenResponse.class
        ).getBody();

        return mapToLoginResponse(token);
    }

    private LoginResponse mapToLoginResponse(KeycloakTokenResponse token) {
        return new LoginResponse(
                200,
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getTokenType(),
                token.getExpiresIn()
        );
    }
}
