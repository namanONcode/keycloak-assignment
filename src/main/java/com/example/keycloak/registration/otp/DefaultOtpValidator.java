package com.example.keycloak.registration.otp;

import org.keycloak.sessions.AuthenticationSessionModel;

public class DefaultOtpValidator implements OtpValidator {
    private final OtpStorage storage;

    public DefaultOtpValidator(OtpStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean isValid(AuthenticationSessionModel session, String inputOtp) {
        String storedOtp = storage.getOtp(session);
        return storedOtp != null && storedOtp.equals(inputOtp);
    }

    @Override
    public boolean hasExceededAttempts(AuthenticationSessionModel session) {
        return storage.getAttempts(session) >= 3;
    }
}
