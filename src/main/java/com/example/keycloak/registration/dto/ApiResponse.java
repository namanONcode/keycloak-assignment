package com.example.keycloak.registration.dto;

public class ApiResponse {
    private final String status;
    private final String message;
    private final String username;

    public ApiResponse(String status, String message, String username) {
        this.status = status;
        this.message = message;
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }
}
