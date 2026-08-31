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

## Execution flow

The custom registration process perfectly mirrors the Keycloak Authentication flow hierarchy. The following diagram illustrates how the Keycloak UI execution steps map directly to our custom Java SPI classes and their core methods:

```mermaid
flowchart TD
    Start((Start Registration)) --> Auth1
    
    subgraph Flow: mobile-otp-registration
        Auth1["<b>Mobile Number Authenticator</b><br/><i>MobileNumberAuthenticator.java</i><br/><small>Prompts for mobile number, validates format & duplicates,<br/>generates and sends OTP.</small>"]
        
        Auth1 -- "On Success" --> Auth2
        
        Auth2["<b>OTP Verification Authenticator</b><br/><i>OtpVerificationAuthenticator.java</i><br/><small>Prompts for OTP, verifies against stored session note,<br/>enforces rate limits and expiration.</small>"]
        
        Auth2 -- "On Success" --> SubFlow
        
        subgraph SubFlow [Sub-flow: mobile-otp-registration-form]
            direction TB
            FormAction1["<b>Mobile Username Form Action</b><br/><i>MobileUsernameFormAction.java</i><br/><small>Injects the verified mobile number as the username<br/>so it doesn't need to be entered on the form.</small>"]
            
            FormAction2["<b>Registration User Profile Creation</b><br/><i>(Built-in Keycloak)</i><br/><small>Creates the base user entity in the database.</small>"]
            
            FormAction3["<b>Custom Profile Validation</b><br/><i>CustomProfileValidation.java</i><br/><small>Validates first/last name, saves them,<br/>and saves mobile number as user attribute.</small>"]
            
            FormAction4["<b>Custom Password Validation</b><br/><i>CustomPasswordValidation.java</i><br/><small>Validates password complexity and confirmation,<br/>then securely hashes and stores it.</small>"]
            
            FormAction1 --> FormAction2 --> FormAction3 --> FormAction4
        end
    end
    
    SubFlow --> End((Registration Complete))
```

### Detailed Method Execution

Here is how the code executes through this flow:

1. **`MobileNumberAuthenticator`**
   - **Phase:** Initial Authentication Step.
   - **Code:** Validates the input mobile number using `MobileValidator`, checks for existing users with that number, generates a secure OTP using `SecureRandomOtpGenerator`, and logs/sends it via `LoggingOtpSender`. It stores the mobile number in the Keycloak `AuthenticationSessionModel` as a note.

2. **`OtpVerificationAuthenticator`**
   - **Phase:** Second Authentication Step.
   - **Code:** Retrieves the generated OTP from the session. It validates the user's input against it, checking for expiration (e.g., 5 minutes) and enforcing brute-force limits (e.g., max 3 attempts).

3. **`MobileUsernameFormAction`**
   - **Phase:** Form Validation (First step of the Registration Form).
   - **Code:** Intercepts the form submission. It pulls the verified mobile number from the session note and injects it into the form data as the `username` field. This allows the username field to remain hidden on the frontend UI.

4. **Registration User Profile Creation** *(Keycloak Native)*
   - **Phase:** Core Keycloak Action.
   - **Code:** This is Keycloak's native action that takes the form data (now containing the injected mobile username) and creates the user in the Keycloak database.

5. **`CustomProfileValidation`**
   - **Phase:** Form Validation / Success Action.
   - **Code:** 
     - `validate()` checks that First Name and Last Name are provided.
     - `success()` updates the newly created user with their First Name, Last Name, and saves the verified mobile number as a permanent Keycloak User Attribute (`user.setSingleAttribute("mobile", mobile)`).

6. **`CustomPasswordValidation`**
   - **Phase:** Form Validation / Success Action.
   - **Code:**
     - `validate()` ensures the password meets complexity rules and matches the confirmation field.
     - `success()` updates the user's Keycloak credentials with the new hashed password.


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

## Testing and OTP retrieval

To test the registration flow and retrieve the generated OTPs, you can either:
- **Use the SDK**: Integrate the API with a client application using the Keycloak SDK to process the registration.
- **Use debug logs**: Run the Keycloak server in development mode (`start-dev`) and monitor the terminal or log files. The `LoggingOtpSender` prints the generated OTPs directly to the console for easy retrieval during testing.
