package com.example.keycloak.registration.otp;

import org.keycloak.sessions.AuthenticationSessionModel;

public interface OtpStorage {
    void storeOtp(AuthenticationSessionModel session, String otp);
    String getOtp(AuthenticationSessionModel session);
    void clearOtp(AuthenticationSessionModel session);
    void incrementAttempt(AuthenticationSessionModel session);
    int getAttempts(AuthenticationSessionModel session);
}
