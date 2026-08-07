package com.example.keycloak.registration.validation;

import com.example.keycloak.registration.dto.RegisterRequest;
import com.example.keycloak.registration.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidatorTest {

    private RegistrationValidator validator;
    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        validator = new RegistrationValidator();
        request = new RegisterRequest();
        request.setUsername("naman");
        request.setPassword("Password@123");
        request.setFirstName("Naman");
        request.setLastName("Jain");
        request.setMobile("1234567890");
    }

    @Test
    void testValidRequest() {
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void testNullRequest() {
        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validate(null));
        assertEquals("Request body cannot be null", exception.getMessage());
    }

    @Test
    void testInvalidUsername() {
        request.setUsername("");
        assertThrows(ValidationException.class, () -> validator.validate(request));

        request.setUsername("ab");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void testInvalidPassword() {
        request.setPassword("");
        assertThrows(ValidationException.class, () -> validator.validate(request));

        request.setPassword("weak");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void testInvalidFirstName() {
        request.setFirstName("");
        assertThrows(ValidationException.class, () -> validator.validate(request));

        request.setFirstName("John123");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void testInvalidLastName() {
        request.setLastName("");
        assertThrows(ValidationException.class, () -> validator.validate(request));

        request.setLastName("Doe123");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }
}
