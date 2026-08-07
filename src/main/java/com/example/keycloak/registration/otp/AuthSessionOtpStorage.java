package com.example.keycloak.registration.otp;

import org.keycloak.sessions.AuthenticationSessionModel;

public class AuthSessionOtpStorage implements OtpStorage {
    private static final String OTP_NOTE = "mobile_otp";
    private static final String OTP_TIME_NOTE = "mobile_otp_time";
    private static final String OTP_ATTEMPTS_NOTE = "mobile_otp_attempts";
    
    @Override
    public void storeOtp(AuthenticationSessionModel session, String otp) {
        session.setAuthNote(OTP_NOTE, otp);
        session.setAuthNote(OTP_TIME_NOTE, String.valueOf(System.currentTimeMillis()));
        session.setAuthNote(OTP_ATTEMPTS_NOTE, "0");
    }
    
    @Override
    public String getOtp(AuthenticationSessionModel session) {
        String timeStr = session.getAuthNote(OTP_TIME_NOTE);
        if (timeStr != null) {
            long time = Long.parseLong(timeStr);
            if (System.currentTimeMillis() - time > 5 * 60 * 1000) {
                return null;
            }
        }
        return session.getAuthNote(OTP_NOTE);
    }
    
    @Override
    public void clearOtp(AuthenticationSessionModel session) {
        session.removeAuthNote(OTP_NOTE);
        session.removeAuthNote(OTP_TIME_NOTE);
        session.removeAuthNote(OTP_ATTEMPTS_NOTE);
    }
    
    @Override
    public void incrementAttempt(AuthenticationSessionModel session) {
        int attempts = getAttempts(session);
        session.setAuthNote(OTP_ATTEMPTS_NOTE, String.valueOf(attempts + 1));
    }
    
    @Override
    public int getAttempts(AuthenticationSessionModel session) {
        String attemptsStr = session.getAuthNote(OTP_ATTEMPTS_NOTE);
        return attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
    }
}
