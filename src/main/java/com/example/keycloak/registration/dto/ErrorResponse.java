package com.example.keycloak.registration.dto;

import java.util.Collections;
import java.util.List;

public class ErrorResponse {
    private final String status;
    private final List<String> errors;

    public ErrorResponse(String status, List<String> errors) {
        this.status = status;
        this.errors = errors;
    }

    public ErrorResponse(String status, String error) {
        this.status = status;
        this.errors = Collections.singletonList(error);
    }

    public String getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}
