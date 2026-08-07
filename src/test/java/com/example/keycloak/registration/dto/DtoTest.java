package com.example.keycloak.registration.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DtoTest {

    @Test
    void testRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        assertNull(request.getUsername());

        request.setUsername("naman");
        request.setPassword("pass");
        request.setFirstName("Naman");
        request.setLastName("Jain");
        request.setMobile("1234567890");

        assertEquals("naman", request.getUsername());
        assertEquals("pass", request.getPassword());
        assertEquals("Naman", request.getFirstName());
        assertEquals("Jain", request.getLastName());
        assertEquals("1234567890", request.getMobile());
    }

    @Test
    void testApiResponse() {
        ApiResponse response = new ApiResponse("SUCCESS", "msg", "naman");
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("msg", response.getMessage());
        assertEquals("naman", response.getUsername());
    }

    @Test
    void testErrorResponse() {
        ErrorResponse response = new ErrorResponse("ERROR", "msg");
        assertEquals("ERROR", response.getStatus());
        assertEquals("msg", response.getErrors().get(0));
    }
}
