package com.example.keycloak.registration.authentication;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.jboss.logging.Logger;
import jakarta.ws.rs.core.MultivaluedMap;

public class MobileUsernameFormAction implements FormAction {

    private static final Logger logger = Logger.getLogger(MobileUsernameFormAction.class);

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        
    }

    @Override
    public void validate(ValidationContext context) {
        String mobile = context.getAuthenticationSession().getAuthNote("mobile_reg_mobile");
        if (mobile != null) {
            logger.info("Injecting mobile number into registration form as username.");
            if (logger.isDebugEnabled()) {
                logger.debugf("Injecting username for mobile %s", mobile);
            }
            MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
            
            formData.putSingle("username", mobile);
            
            formData.putSingle("email", mobile + "@example.com");
        }
        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        if (user != null) {
            String mobile = context.getAuthenticationSession().getAuthNote("mobile_reg_mobile");
            if (mobile != null) {
                user.setSingleAttribute("mobile", mobile);
            }
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
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
