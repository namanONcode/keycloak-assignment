package com.example.keycloak.registration.service;

import com.example.keycloak.registration.dto.ApiResponse;
import com.example.keycloak.registration.dto.RegisterRequest;
import com.example.keycloak.registration.exception.DuplicateUserException;
import com.example.keycloak.registration.validation.RegistrationValidator;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;

import java.util.stream.Stream;

public class RegistrationService {

    private static final Logger logger = Logger.getLogger(RegistrationService.class);
    
    private final KeycloakSession session;
    private final RegistrationValidator validator;

    public RegistrationService(KeycloakSession session) {
        this.session = session;
        this.validator = new RegistrationValidator();
    }

    public Response registerUser(RegisterRequest request) {
        validator.validate(request);

        RealmModel realm = session.getContext().getRealm();

        checkDuplicateUsername(realm, request.getUsername());
        checkDuplicateMobile(realm, request.getMobile());

        UserModel user = createUser(realm, request);

        logger.info("Successfully registered user");
        if (logger.isDebugEnabled()) {
            logger.debugf("Successfully registered user with username: %s", request.getUsername());
        }
        
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse("SUCCESS", "User registered successfully", user.getUsername()))
                .build();
    }

    private void checkDuplicateUsername(RealmModel realm, String username) {
        UserModel existingUser = session.users().getUserByUsername(realm, username);
        if (existingUser != null) {
            logger.warn("Registration failed: Username is already taken");
            if (logger.isDebugEnabled()) {
                logger.debugf("Duplicate username attempt: %s", username);
            }
            throw new DuplicateUserException("Username '" + username + "' is already taken");
        }
    }

    private void checkDuplicateMobile(RealmModel realm, String mobile) {
        try (Stream<UserModel> users = session.users().searchForUserByUserAttributeStream(realm, "mobile", mobile)) {
            if (users.findAny().isPresent()) {
                logger.warn("Registration failed: Mobile is already in use");
                if (logger.isDebugEnabled()) {
                    logger.debugf("Duplicate mobile attempt: %s", mobile);
                }
                throw new DuplicateUserException("Mobile number '" + mobile + "' is already in use");
            }
        }
    }

    private UserModel createUser(RealmModel realm, RegisterRequest request) {
        UserModel user = session.users().addUser(realm, request.getUsername());
        user.setEnabled(true);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setSingleAttribute("mobile", request.getMobile());

        user.credentialManager().updateCredential(
                UserCredentialModel.password(request.getPassword(), false)
        );

        return user;
    }
}
