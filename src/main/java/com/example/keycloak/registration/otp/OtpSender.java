package com.example.keycloak.registration.otp;

public interface OtpSender {
    void sendOtp(String mobile, String otp);
}
