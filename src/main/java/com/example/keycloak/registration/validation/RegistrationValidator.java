package com.example.keycloak.registration.validation;

import com.example.keycloak.registration.dto.RegisterRequest;
import com.example.keycloak.registration.exception.ValidationException;
import com.example.keycloak.registration.util.MobileValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegistrationValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]{1,100}$");

    public void validate(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Request body cannot be null");
        }

        List<String> errors = new ArrayList<>();

        validateUsername(request.getUsername(), errors);
        validatePassword(request.getPassword(), errors);
        validateFirstName(request.getFirstName(), errors);
        validateLastName(request.getLastName(), errors);
        MobileValidator.validate(request.getMobile(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public void validateUsername(String username, List<String> errors) {
        if (username == null || username.trim().isEmpty()) {
            errors.add("Username is required");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.add("Username must be between 3 and 50 characters and contain only letters, numbers, and underscores");
        }
    }

    public void validatePassword(String password, List<String> errors) {
        if (password == null || password.trim().isEmpty()) {
            errors.add("Password is required");
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            errors.add("Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
    }

    public void validateFirstName(String firstName, List<String> errors) {
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.add("First name is required");
        } else if (!NAME_PATTERN.matcher(firstName).matches()) {
            errors.add("First name must be between 1 and 100 characters and contain only alphabets");
        }
    }

    public void validateLastName(String lastName, List<String> errors) {
        if (lastName == null || lastName.trim().isEmpty()) {
            errors.add("Last name is required");
        } else if (!NAME_PATTERN.matcher(lastName).matches()) {
            errors.add("Last name must be between 1 and 100 characters and contain only alphabets");
        }
    }
}
