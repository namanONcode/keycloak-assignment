package com.example.keycloak.registration.authentication;

import com.example.keycloak.registration.validation.RegistrationValidator;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.utils.FormMessage;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class CustomPasswordValidation implements FormAction {

    private final RegistrationValidator validator = new RegistrationValidator();

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        form.setAttribute("passwordRequired", true);
    }

    @Override
    public void validate(ValidationContext context) {
        context.success();
    }

    @Override
    public void success(FormContext context) {
        
        
        
        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String password = formData.getFirst("password");
        
        try {
            user.credentialManager().updateCredential(UserCredentialModel.password(password));
        } catch (Exception e) {
            
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

    @Override
    public void close() {}
}
