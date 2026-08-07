package com.example.keycloak.registration.provider;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import com.example.keycloak.registration.resource.RegistrationResource;


public class RegistrationRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public RegistrationRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new RegistrationResource(session);
    }

    @Override
    public void close() {
        
    }
}
