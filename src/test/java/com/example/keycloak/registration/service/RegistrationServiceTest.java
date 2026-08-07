package com.example.keycloak.registration.service;

import com.example.keycloak.registration.dto.ApiResponse;
import com.example.keycloak.registration.dto.RegisterRequest;
import com.example.keycloak.registration.exception.DuplicateUserException;
import com.example.keycloak.registration.exception.ValidationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.*;
import org.keycloak.credential.CredentialModel;
import org.mockito.ArgumentCaptor;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    private KeycloakSession session;
    private KeycloakContext context;
    private RealmModel realm;
    private UserProvider userProvider;
    private RegistrationService service;
    private RegisterRequest request;
    private UserModel userModel;
    private SubjectCredentialManager credentialManager;

    @BeforeEach
    void setUp() {
        session = mock(KeycloakSession.class);
        context = mock(KeycloakContext.class);
        realm = mock(RealmModel.class);
        userProvider = mock(UserProvider.class);
        userModel = mock(UserModel.class);
        credentialManager = mock(SubjectCredentialManager.class);

        when(session.getContext()).thenReturn(context);
        when(context.getRealm()).thenReturn(realm);
        when(session.users()).thenReturn(userProvider);
        
        when(userModel.getUsername()).thenReturn("john");
        when(userModel.credentialManager()).thenReturn(credentialManager);

        service = new RegistrationService(session);

        request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("Password@123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setMobile("9876543210");
    }

    @Test
    void testRegisterUserSuccess() {
        when(userProvider.getUserByUsername(realm, "john")).thenReturn(null);
        when(userProvider.searchForUserByUserAttributeStream(realm, "mobile", "9876543210")).thenReturn(Stream.empty());
        when(userProvider.addUser(realm, "john")).thenReturn(userModel);

        Response response = service.registerUser(request);

        assertEquals(201, response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertEquals("SUCCESS", apiResponse.getStatus());
        assertEquals("john", apiResponse.getUsername());

        verify(userModel).setEnabled(true);
        verify(userModel).setFirstName("John");
        verify(userModel).setLastName("Doe");
        verify(userModel).setSingleAttribute("mobile", "9876543210");
        verify(credentialManager).updateCredential(any(UserCredentialModel.class));
    }

    @Test
    void testRegisterUserDuplicateUsername() {
        when(userProvider.getUserByUsername(realm, "john")).thenReturn(userModel);

        assertThrows(DuplicateUserException.class, () -> service.registerUser(request));
    }

    @Test
    void testRegisterUserDuplicateMobile() {
        when(userProvider.getUserByUsername(realm, "john")).thenReturn(null);
        when(userProvider.searchForUserByUserAttributeStream(realm, "mobile", "9876543210")).thenReturn(Stream.of(userModel));

        assertThrows(DuplicateUserException.class, () -> service.registerUser(request));
    }

    @Test
    void testRegisterUserValidationError() {
        request.setUsername(""); 
        assertThrows(ValidationException.class, () -> service.registerUser(request));
    }
}
