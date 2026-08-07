package com.example.keycloak.registration.otp;

import org.keycloak.sessions.AuthenticationSessionModel;

public class DefaultOtpService implements OtpService {
    private final OtpGenerator generator;
    private final OtpSender sender;
    private final OtpStorage storage;
    private final OtpValidator validator;
    
    public DefaultOtpService() {
        this.generator = new SecureRandomOtpGenerator();
        this.sender = new LoggingOtpSender();
        this.storage = new AuthSessionOtpStorage();
        this.validator = new DefaultOtpValidator(this.storage);
    }
    
    @Override
    public void processOtpGeneration(AuthenticationSessionModel session, String mobile) {
        String otp = generator.generateOtp();
        storage.storeOtp(session, otp);
        sender.sendOtp(mobile, otp);
    }
    
    @Override
    public boolean processOtpVerification(AuthenticationSessionModel session, String inputOtp) {
        storage.incrementAttempt(session);
        return validator.isValid(session, inputOtp);
    }
    
    @Override
    public boolean hasExceededAttempts(AuthenticationSessionModel session) {
        return validator.hasExceededAttempts(session);
    }
    
    @Override
    public void invalidateOtp(AuthenticationSessionModel session) {
        storage.clearOtp(session);
    }
}
