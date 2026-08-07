package com.example.keycloak.registration.otp;

import org.keycloak.sessions.AuthenticationSessionModel;

public interface OtpService {
    void processOtpGeneration(AuthenticationSessionModel session, String mobile);
    boolean processOtpVerification(AuthenticationSessionModel session, String inputOtp);
    boolean hasExceededAttempts(AuthenticationSessionModel session);
    void invalidateOtp(AuthenticationSessionModel session);
}
