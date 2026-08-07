package com.example.keycloak.registration.resource;

import org.keycloak.models.KeycloakSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.FormAction;

public class DebugResource {
    private final KeycloakSession session;

    public DebugResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("auths")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAuths() {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(Authenticator.class)
                .map(f -> f.getId())
                .collect(Collectors.toList());
    }

    @GET
    @Path("forms")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getForms() {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(FormAction.class)
                .map(f -> f.getId())
                .collect(Collectors.toList());
    }
}
