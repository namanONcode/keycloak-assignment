# Keycloak custom user registration SPI

This repository contains a Keycloak Service Provider Interface (SPI) plugin that implements a custom user registration process. It provides a REST endpoint for user registration and a browser-based OTP flow that uses a mobile number as the username.

## Assignment details

The implementation covers two specific sets of requirements:

**Assignment 1: Custom REST endpoint**
- A `POST /realms/demo/custom-register/register` endpoint.
- Aggregated validation rules returning all errors simultaneously.
- Duplicate checks for usernames and mobile numbers.

**Assignment 2: Mobile OTP registration flow**
- Extends the built-in registration flow using Keycloak's Authenticator and FormAction SPIs.
- Validates the mobile number and generates an OTP.
- Verifies the OTP before presenting the registration form.
- Hides the username and email fields on the form.
- Injects the verified mobile number as the username before user creation.

## Implementation details

The SPI includes several components to handle validation, authentication, and user creation.

### Rate limiter and OTP verification

The `OtpVerificationAuthenticator` manages OTP input and validation. It enforces a rate limit by restricting users to three incorrect attempts. After three failures, the user must restart the flow and request a new OTP. The OTP is stored in the `AuthenticationSessionModel` and expires after five minutes.

### Improved logging techniques

The `LoggingOtpSender` handles OTP delivery by printing generated codes to the Keycloak server logs. It uses the `org.jboss.logging.Logger` interface for direct integration with Keycloak's native logging system. The application uses parameterized log messages (like `logger.debugf`) instead of string concatenation to improve performance and prevent unnecessary string allocation during high-volume registration events. 

### User creation and attribute persistence

The built-in Keycloak form actions were replaced to manage the data flow correctly. The execution priorities are arranged so the `MobileUsernameFormAction` runs first to inject the mobile number as the username. The built-in `RegistrationUserCreation` action creates the user entity. Finally, `CustomProfileValidation` reads the mobile number from the authentication session and saves it as a user attribute. 

### Testing and CI pipeline

The project uses JUnit and Mockito for unit testing. A `mock-maker-subclass` configuration allows Mockito to test `KeycloakSession` objects cleanly on JDK 21+. The `jacoco-maven-plugin` enforces an 80% instruction coverage minimum on all core logic and validation services. A GitHub Actions workflow automatically builds the project and verifies test coverage on pull requests.

## Build and deployment

1. Build the project using Maven:
   ```bash
   mvn clean package
   ```
2. Copy the resulting JAR file to the Keycloak providers directory:
   ```bash
   cp target/keycloak-custom-registration-1.0.0-SNAPSHOT.jar /path/to/keycloak/providers/
   ```
3. Start Keycloak in development mode:
   ```bash
   bin/kc.sh start-dev
   ```

## Configuration

1. Create a Generic authentication flow named `Mobile OTP Registration`.
2. Add the `Mobile Number Authenticator` and set it to Required.
3. Add the `OTP Verification Authenticator` and set it to Required.
4. Add the `Registration Page` subflow and set it to Required.
5. Inside the subflow, add `Custom Profile Validation`, `Custom Password Validation`, `Mobile Username Form Action`, and `Registration User Creation`. Set all to Required.
6. Bind the flow to the realm's Registration setting.
7. Change the realm's login theme to `mobile-registration`.

Alternatively, use the custom `POST /setup-flow` endpoint included in the REST resource to configure the flow structure programmatically.
