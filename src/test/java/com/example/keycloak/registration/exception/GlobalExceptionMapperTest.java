package com.example.keycloak.registration.exception;

import com.example.keycloak.registration.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionMapperTest {

    private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

    @Test
    void testValidationException() {
        Response response = mapper.toResponse(new ValidationException("Validation failed"));
        assertEquals(400, response.getStatus());
        ErrorResponse entity = (ErrorResponse) response.getEntity();
        assertEquals("FAILED", entity.getStatus());
        assertEquals("Validation failed", entity.getErrors().get(0));
    }

    @Test
    void testDuplicateUserException() {
        Response response = mapper.toResponse(new DuplicateUserException("Duplicate found"));
        assertEquals(409, response.getStatus());
        ErrorResponse entity = (ErrorResponse) response.getEntity();
        assertEquals("FAILED", entity.getStatus());
        assertEquals("Duplicate found", entity.getErrors().get(0));
    }

    @Test
    void testGenericException() {
        Response response = mapper.toResponse(new RuntimeException("Something went wrong"));
        assertEquals(500, response.getStatus());
        ErrorResponse entity = (ErrorResponse) response.getEntity();
        assertEquals("FAILED", entity.getStatus());
    }
}
