package com.example.keycloak.registration.util;

import com.example.keycloak.registration.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MobileValidatorTest {

    @Test
    void testValidMobile() {
        assertDoesNotThrow(() -> MobileValidator.validate("9876543210"));
    }

    @Test
    void testNullMobile() {
        ValidationException exception = assertThrows(ValidationException.class, () -> MobileValidator.validate(null));
        assertEquals("Mobile number is required", exception.getMessage());
    }

    @Test
    void testEmptyMobile() {
        ValidationException exception = assertThrows(ValidationException.class, () -> MobileValidator.validate("   "));
        assertEquals("Mobile number is required", exception.getMessage());
    }

    @Test
    void testInvalidMobile() {
        ValidationException exception = assertThrows(ValidationException.class, () -> MobileValidator.validate("123456789"));
        assertEquals("Mobile number must be exactly 10 digits", exception.getMessage());
        
        exception = assertThrows(ValidationException.class, () -> MobileValidator.validate("abcdefghij"));
        assertEquals("Mobile number must be exactly 10 digits", exception.getMessage());
    }
}
