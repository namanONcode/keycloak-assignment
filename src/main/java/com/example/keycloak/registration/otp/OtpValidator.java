package com.example.keycloak.registration.otp;

import org.keycloak.sessions.AuthenticationSessionModel;

public interface OtpValidator {
    boolean isValid(AuthenticationSessionModel session, String inputOtp);
    boolean hasExceededAttempts(AuthenticationSessionModel session);
}
