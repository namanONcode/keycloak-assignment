package com.example.keycloak.registration.resource;

import com.example.keycloak.registration.dto.RegisterRequest;
import com.example.keycloak.registration.exception.GlobalExceptionMapper;
import com.example.keycloak.registration.service.RegistrationService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.RealmModel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.keycloak.registration.dto.ErrorResponse;
import java.util.Collections;

public class RegistrationResource {

    private static class RateLimitInfo {
        final AtomicInteger count = new AtomicInteger(1);
        long windowStart = System.currentTimeMillis();
    }
    
    private static final ConcurrentHashMap<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();
    private static final AtomicInteger cleanupCounter = new AtomicInteger(0);
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_MS = 60000;

    private static final Logger logger = Logger.getLogger(RegistrationResource.class);
    
    private final KeycloakSession session;
    private final RegistrationService registrationService;
    private final GlobalExceptionMapper exceptionMapper;

    public RegistrationResource(KeycloakSession session) {
        this.session = session;
        this.registrationService = new RegistrationService(session);
        this.exceptionMapper = new GlobalExceptionMapper();
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(RegisterRequest request) {
        String ipAddress = "unknown";
        if (session.getContext().getConnection() != null) {
            ipAddress = session.getContext().getConnection().getRemoteAddr();
        }
        
        String username = request != null ? request.getUsername() : "unknown";
        String mobile = request != null ? request.getMobile() : "unknown";
        
        logger.info("Received custom registration request");
        if (logger.isDebugEnabled()) {
            logger.debugf("Registration request details - IP: %s, username: %s, mobile: %s", ipAddress, username, mobile);
        }
        
        if (isRateLimited(ipAddress)) {
            logger.warn("Rate limit exceeded for IP.");
            if (logger.isDebugEnabled()) {
                logger.debugf("Rate limit exceeded for IP: %s", ipAddress);
            }
            ErrorResponse errorResponse = new ErrorResponse("Too Many Requests", Collections.singletonList("Rate limit exceeded. Please try again later."));
            return Response.status(429).entity(errorResponse).build();
        }
        
        try {
            return registrationService.registerUser(request);
        } catch (RuntimeException e) {
            return exceptionMapper.toResponse(e);
        }
    }
    
    private boolean isRateLimited(String ipAddress) {
        long now = System.currentTimeMillis();
        
        
        if (cleanupCounter.incrementAndGet() % 1000 == 0) {
            rateLimits.entrySet().removeIf(entry -> now - entry.getValue().windowStart > WINDOW_MS);
        }
        
        RateLimitInfo info = rateLimits.compute(ipAddress, (ip, currentInfo) -> {
            if (currentInfo == null || now - currentInfo.windowStart > WINDOW_MS) {
                RateLimitInfo newInfo = new RateLimitInfo();
                newInfo.windowStart = now;
                return newInfo;
            }
            currentInfo.count.incrementAndGet();
            return currentInfo;
        });
        
        return info.count.get() > MAX_REQUESTS_PER_MINUTE;
    }

    @GET
    @Path("debug/auths")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<String> getAuths() {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(org.keycloak.authentication.Authenticator.class)
                .map(f -> f.getId())
                .collect(java.util.stream.Collectors.toList());
    }

    @GET
    @Path("debug/forms")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<String> getForms() {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(org.keycloak.authentication.FormAction.class)
                .map(f -> f.getId())
                .collect(java.util.stream.Collectors.toList());
    }

    @POST
    @Path("setup-flow")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setupFlow() {
        RealmModel realm = session.getContext().getRealm();
        
        
        AuthenticationFlowModel existingFlow = realm.getFlowByAlias("mobile-otp-registration");
        if (existingFlow != null) {
            if (realm.getRegistrationFlow() != null && existingFlow.getId().equals(realm.getRegistrationFlow().getId())) {
                realm.setRegistrationFlow(realm.getFlowByAlias("registration"));
            }
            realm.removeAuthenticationFlow(existingFlow);
        }
        
        
        AuthenticationFlowModel flow = new AuthenticationFlowModel();
        flow.setAlias("mobile-otp-registration");
        flow.setDescription("Mobile OTP Registration Flow");
        flow.setProviderId("basic-flow");
        flow.setTopLevel(true);
        flow.setBuiltIn(false);
        flow = realm.addAuthenticationFlow(flow);
        
        
        AuthenticationExecutionModel mobileAuth = new AuthenticationExecutionModel();
        mobileAuth.setParentFlow(flow.getId());
        mobileAuth.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        mobileAuth.setAuthenticator("mobile-number-authenticator");
        mobileAuth.setPriority(10);
        mobileAuth.setAuthenticatorFlow(false);
        realm.addAuthenticatorExecution(mobileAuth);
        
        
        AuthenticationExecutionModel otpAuth = new AuthenticationExecutionModel();
        otpAuth.setParentFlow(flow.getId());
        otpAuth.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        otpAuth.setAuthenticator("otp-verification-authenticator");
        otpAuth.setPriority(20);
        otpAuth.setAuthenticatorFlow(false);
        realm.addAuthenticatorExecution(otpAuth);
        
        
        AuthenticationFlowModel formFlow = new AuthenticationFlowModel();
        formFlow.setAlias("mobile-otp-registration-form");
        formFlow.setDescription("Registration Form Subflow");
        formFlow.setProviderId("form-flow");
        formFlow.setTopLevel(false);
        formFlow.setBuiltIn(false);
        formFlow = realm.addAuthenticationFlow(formFlow);
        
        AuthenticationExecutionModel formExecution = new AuthenticationExecutionModel();
        formExecution.setParentFlow(flow.getId());
        formExecution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        formExecution.setFlowId(formFlow.getId());
        formExecution.setPriority(30);
        formExecution.setAuthenticatorFlow(true);
        formExecution.setAuthenticator("registration-page-form");
        realm.addAuthenticatorExecution(formExecution);
        
        
        AuthenticationExecutionModel profileVal = new AuthenticationExecutionModel();
        profileVal.setParentFlow(formFlow.getId());
        profileVal.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        profileVal.setAuthenticator("custom-profile-validation");
        profileVal.setPriority(30);
        profileVal.setAuthenticatorFlow(false);
        realm.addAuthenticatorExecution(profileVal);
        
        AuthenticationExecutionModel passwordVal = new AuthenticationExecutionModel();
        passwordVal.setParentFlow(formFlow.getId());
        passwordVal.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        passwordVal.setAuthenticator("custom-password-validation");
        passwordVal.setPriority(40);
        passwordVal.setAuthenticatorFlow(false);
        realm.addAuthenticatorExecution(passwordVal);
        
        AuthenticationExecutionModel usernameAction = new AuthenticationExecutionModel();
        usernameAction.setParentFlow(formFlow.getId());
        usernameAction.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        usernameAction.setAuthenticator("mobile-username-form-action");
        usernameAction.setPriority(10);
        usernameAction.setAuthenticatorFlow(false);
        realm.addAuthenticatorExecution(usernameAction);
        
        AuthenticationExecutionModel userCreation = new AuthenticationExecutionModel();
        userCreation.setParentFlow(formFlow.getId());
        userCreation.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
        userCreation.setAuthenticator("registration-user-creation");
        userCreation.setPriority(20);
        userCreation.setAuthenticatorFlow(false);
        realm.addAuthenticatorExecution(userCreation);
        
        
        realm.setRegistrationFlow(flow);
        realm.setLoginTheme("mobile-registration");
        realm.setRegistrationAllowed(true);
        
        return Response.ok("{\"status\":\"success\", \"message\":\"Flow configured successfully entirely via Java!\"}").build();
    }
}
