package com.example.keycloak.registration.util;

import com.example.keycloak.registration.exception.ValidationException;
import java.util.List;
import java.util.regex.Pattern;

public class MobileValidator {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10}$");

    private MobileValidator() {
        
    }

    public static void validate(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new ValidationException("Mobile number is required");
        }
        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new ValidationException("Mobile number must be exactly 10 digits");
        }
    }
    
    public static void validate(String mobile, List<String> errors) {
        if (mobile == null || mobile.trim().isEmpty()) {
            errors.add("Mobile number is required");
        } else if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            errors.add("Mobile number must be exactly 10 digits");
        }
    }
}
