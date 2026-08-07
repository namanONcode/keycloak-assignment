package com.example.keycloak.registration.provider;

import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RegistrationRealmResourceProviderFactoryTest {

    @Test
    void testFactory() {
        RegistrationRealmResourceProviderFactory factory = new RegistrationRealmResourceProviderFactory();
        assertEquals("custom-register", factory.getId());
        
        KeycloakSession session = mock(KeycloakSession.class);
        assertNotNull(factory.create(session));
        
        assertDoesNotThrow(() -> factory.init(null));
        assertDoesNotThrow(() -> factory.postInit(null));
        assertDoesNotThrow(factory::close);
    }
}
