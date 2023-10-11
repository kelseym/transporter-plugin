package org.nrg.xnatx.plugins.transporter.exceptions;

import java.util.Map;

public class ValidationException extends Exception {

    private final Map<String, Object> validationErrors;

    public ValidationException(final String message, final Map<String, Object> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map getValidationErrors() {
        return validationErrors;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        message.append("\nValidation Errors:\n");
        if (validationErrors != null) {
            for (Map.Entry<String, Object> entry : validationErrors.entrySet()) {
                message.append(entry.getKey()).append(": ").append(entry.toString()).append("\n");
            }
        }
        return message.toString();
    }
}
