package com.example.keycloak.registration.otp;

import org.jboss.logging.Logger;

public class LoggingOtpSender implements OtpSender {
    private static final Logger logger = Logger.getLogger(LoggingOtpSender.class);

    @Override
    public void sendOtp(String mobile, String otp) {
        logger.infof("Generated OTP for mobile %s: OTP=%s", mobile, otp);
    }
}
