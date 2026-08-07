package com.example.keycloak.registration.resource;

import com.example.keycloak.registration.dto.RegisterRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.SubjectCredentialManager;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationResourceTest {

    private KeycloakSession session;
    private RegistrationResource resource;
    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        session = mock(KeycloakSession.class);
        KeycloakContext context = mock(KeycloakContext.class);
        ClientConnection connection = mock(ClientConnection.class);
        
        when(session.getContext()).thenReturn(context);
        when(context.getConnection()).thenReturn(connection);
        when(connection.getRemoteAddr()).thenReturn("127.0.0.1");
        
        RealmModel realm = mock(RealmModel.class);
        when(context.getRealm()).thenReturn(realm);
        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        when(userProvider.getUserByUsername(realm, "john")).thenReturn(null);
        when(userProvider.searchForUserByUserAttributeStream(realm, "mobile", "9876543210")).thenReturn(Stream.empty());
        
        UserModel userModel = mock(UserModel.class);
        when(userProvider.addUser(realm, "john")).thenReturn(userModel);
        when(userModel.getUsername()).thenReturn("john");
        SubjectCredentialManager credentialManager = mock(SubjectCredentialManager.class);
        when(userModel.credentialManager()).thenReturn(credentialManager);

        resource = new RegistrationResource(session);

        request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("Password@123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setMobile("9876543210");
    }

    @Test
    void testRegisterUserSuccess() {
        Response response = resource.registerUser(request);
        assertEquals(201, response.getStatus());
    }
    
    @Test
    void testRegisterUserNullConnection() {
        when(session.getContext().getConnection()).thenReturn(null);
        Response response = resource.registerUser(request);
        assertEquals(201, response.getStatus());
    }

    @Test
    void testRegisterUserValidationErrorCaught() {
        request.setUsername(""); 
        Response response = resource.registerUser(request);
        assertEquals(400, response.getStatus());
    }
    
    @Test
    void testRegisterUserDuplicateCaught() {
        UserProvider userProvider = session.users();
        when(userProvider.getUserByUsername(session.getContext().getRealm(), "john")).thenReturn(mock(UserModel.class));
        Response response = resource.registerUser(request);
        assertEquals(409, response.getStatus());
    }
}
