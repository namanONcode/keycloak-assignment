package com.example.keycloak.registration.otp;

import java.security.SecureRandom;

public class SecureRandomOtpGenerator implements OtpGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }
}
