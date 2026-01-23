package com.andreyk.practiceproject.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
