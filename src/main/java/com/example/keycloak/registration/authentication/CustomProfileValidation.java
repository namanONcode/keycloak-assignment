package com.example.keycloak.registration.authentication;

import com.example.keycloak.registration.validation.RegistrationValidator;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class CustomProfileValidation implements FormAction {

    private final RegistrationValidator validator = new RegistrationValidator();

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {}

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();
        List<String> stringErrors = new ArrayList<>();

        String firstName = formData.getFirst("firstName");
        String lastName = formData.getFirst("lastName");
        String password = formData.getFirst("password");
        String passwordConfirm = formData.getFirst("password-confirm");

        validator.validateFirstName(firstName, stringErrors);
        validator.validateLastName(lastName, stringErrors);
        validator.validatePassword(password, stringErrors);

        if (password != null && !password.equals(passwordConfirm)) {
            stringErrors.add("Password confirmation doesn't match.");
        }

        if (!stringErrors.isEmpty()) {
            for (String error : stringErrors) {
                errors.add(new FormMessage("customError", error));
            }
            context.validationError(formData, errors);
            return;
        }

        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        user.setFirstName(formData.getFirst("firstName"));
        user.setLastName(formData.getFirst("lastName"));
        
        String mobile = context.getAuthenticationSession().getAuthNote("mobile_reg_mobile");
        if (mobile != null) {
            user.setSingleAttribute("mobile", mobile);
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
