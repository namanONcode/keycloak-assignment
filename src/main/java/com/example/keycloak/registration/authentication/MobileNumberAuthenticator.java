package com.example.keycloak.registration.authentication;

import com.example.keycloak.registration.otp.DefaultOtpService;
import com.example.keycloak.registration.otp.OtpService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import com.example.keycloak.registration.util.MobileValidator;
import org.jboss.logging.Logger;

public class MobileNumberAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(MobileNumberAuthenticator.class);
    private final OtpService otpService;
    
    public MobileNumberAuthenticator() {
        this.otpService = new DefaultOtpService();
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String mobile = authSession.getAuthNote("mobile_reg_mobile");
        if (mobile != null && authSession.getAuthNote("mobile_reg_verified") != null) {
            context.success();
            return;
        }
        Response form = context.form().createForm("mobile-registration.ftl");
        context.challenge(form);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String mobile = formData.getFirst("mobile");
        
        List<String> errors = new ArrayList<>();
        MobileValidator.validate(mobile, errors);
        
        if (!errors.isEmpty()) {
            logger.warn("Mobile validation failed");
            if (logger.isDebugEnabled()) {
                logger.debugf("Validation errors for mobile %s: %s", mobile, errors);
            }
            Response form = context.form()
                .setAttribute("errors", errors)
                .createForm("mobile-registration.ftl");
            context.challenge(form);
            return;
        }
        
        UserModel existingUser = context.getSession().users().getUserByUsername(context.getRealm(), mobile);
        if (existingUser != null) {
            logger.warn("Registration attempt with an already registered mobile number");
            if (logger.isDebugEnabled()) {
                logger.debugf("Mobile number already registered: %s", mobile);
            }
            errors.add("Mobile number is already registered.");
            Response form = context.form()
                .setAttribute("errors", errors)
                .createForm("mobile-registration.ftl");
            context.challenge(form);
            return;
        }
        
        logger.info("Mobile number successfully validated. Generating OTP.");
        if (logger.isDebugEnabled()) {
            logger.debugf("Processing OTP generation for mobile: %s", mobile);
        }
        
        authSession.setAuthNote("mobile_reg_mobile", mobile);
        otpService.processOtpGeneration(authSession, mobile);
        
        context.success();
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
