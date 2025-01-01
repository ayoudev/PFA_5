package com.pfa.backend.security.Auth;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthenticationResponse {
    private String token;
    private String role;

    public AuthenticationResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }
    public AuthenticationResponse() {

    }
    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRole(String role) {
        this.role = role;
    }
}