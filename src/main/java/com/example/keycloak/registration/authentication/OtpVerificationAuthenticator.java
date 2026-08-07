package com.example.keycloak.registration.authentication;

import com.example.keycloak.registration.otp.DefaultOtpService;
import com.example.keycloak.registration.otp.OtpService;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class OtpVerificationAuthenticator implements Authenticator {
    private static final Logger logger = Logger.getLogger(OtpVerificationAuthenticator.class);
    private final OtpService otpService;
    
    public OtpVerificationAuthenticator() {
        this.otpService = new DefaultOtpService();
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        if (authSession.getAuthNote("mobile_reg_verified") != null) {
            context.success();
            return;
        }
        Response form = context.form().createForm("otp-verification.ftl");
        context.challenge(form);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String otp = formData.getFirst("otp");
        
        List<String> errors = new ArrayList<>();
        
        if (otpService.hasExceededAttempts(authSession)) {
            errors.add("Maximum OTP attempts exceeded. Please start again.");
            context.getEvent().error("max_otp_attempts");
            authSession.removeAuthNote("mobile_reg_mobile");
            context.resetFlow();
            return;
        }
        
        if (otpService.processOtpVerification(authSession, otp)) {
            otpService.invalidateOtp(authSession);
            authSession.setAuthNote("mobile_reg_verified", "true");
            
            context.getEvent().clone().event(EventType.CUSTOM_REQUIRED_ACTION).detail("custom_event", "OTP_SUCCESS").success();
            logger.info("OTP verified successfully");
            
            context.success();
        } else {
            errors.add("Invalid or expired OTP.");
            context.getEvent().clone().event(EventType.CUSTOM_REQUIRED_ACTION).detail("custom_event", "OTP_FAILURE").error("invalid_otp");
            logger.warn("OTP verification failed");
            
            Response form = context.form()
                .setAttribute("errors", errors)
                .createForm("otp-verification.ftl");
            context.challenge(form);
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
