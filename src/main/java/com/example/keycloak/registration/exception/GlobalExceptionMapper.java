package com.example.keycloak.registration.exception;

import com.example.keycloak.registration.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof ValidationException) {
            ValidationException ve = (ValidationException) exception;
            logger.warn("Validation failed: " + ve.getErrors());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("FAILED", ve.getErrors()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (exception instanceof DuplicateUserException) {
            logger.warn("Duplicate user detected: " + exception.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("FAILED", exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        
        if (exception instanceof jakarta.ws.rs.WebApplicationException) {
            return ((jakarta.ws.rs.WebApplicationException) exception).getResponse();
        }
        if (exception.getClass().getName().startsWith("org.keycloak")) {
            return Response.status(500).entity(exception.getMessage()).build();
        }

        logger.error("Internal server error", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("FAILED", "An unexpected error occurred"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
