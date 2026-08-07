package com.example.keycloak.registration.provider;

import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RegistrationRealmResourceProviderTest {

    @Test
    void testGetResource() {
        KeycloakSession session = mock(KeycloakSession.class);
        RegistrationRealmResourceProvider provider = new RegistrationRealmResourceProvider(session);
        assertNotNull(provider.getResource());
        assertDoesNotThrow(provider::close);
    }
}
