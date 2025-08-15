package com.lemicare.payment.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception thrown when a specific resource cannot be found in the database.
 * <p>
 * This is an "unchecked" exception (it extends RuntimeException), meaning it does not
 * need to be declared in method signatures. It is designed to be caught by a global
 * exception handler, which will translate it into an HTTP 404 Not Found response.
 * The @ResponseStatus annotation provides a fallback if no specific handler is found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * This is the most common constructor to use.
     *
     * @param message The detail message (e.g., "Supplier with ID 123 not found.").
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     * Useful for wrapping a lower-level exception while providing a more user-friendly message.
     *
     * @param message The detail message.
     * @param cause The original exception that caused this one.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
