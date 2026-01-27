package com.andreyk.practiceproject.core.service.impl;

import com.andreyk.practiceproject.api.dto.auth.KeycloakTokenResponse;
import com.andreyk.practiceproject.api.dto.auth.LoginResponse;
import com.andreyk.practiceproject.core.service.KeycloakClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KeycloakClientImpl implements KeycloakClient {

    private final RestTemplate restTemplate;

    @Value("${KEYCLOAK_URL}")
    private String url;

    @Value("${KEYCLOAK_CLIENT_ID}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;


    @Override
    public LoginResponse authenticate(String username, String password) {
        HttpHeaders headers = getHttpHeaders();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        extractedBaseBody(body);
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        KeycloakTokenResponse token = getKeycloakTokenResponse(request);

        return mapToLoginResponse(token);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        HttpHeaders headers = getHttpHeaders();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        extractedBaseBody(body);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        KeycloakTokenResponse token = getKeycloakTokenResponse(request);

        return mapToLoginResponse(token);
    }

    private void extractedBaseBody(MultiValueMap<String, String> body) {
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private KeycloakTokenResponse getKeycloakTokenResponse(HttpEntity<MultiValueMap<String, String>> request) {
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                KeycloakTokenResponse.class
        ).getBody();
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
