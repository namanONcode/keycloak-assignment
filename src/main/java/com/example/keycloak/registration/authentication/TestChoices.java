package com.example.keycloak.registration.authentication;

import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;

public class TestChoices {
    public static void main(String[] args) {
        AuthenticationExecutionModel.Requirement[] choices = AuthenticatorFactory.REQUIREMENT_CHOICES;
        if (choices == null) {
            System.out.println("CHOICES IS NULL");
        } else {
            System.out.println("CHOICES LENGTH: " + choices.length);
        }
    }
}
