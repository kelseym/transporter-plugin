package org.nrg.xnatx.plugins.transporter.exceptions;

import org.nrg.xnatx.plugins.transporter.model.SnapItem;

import java.util.Map;

public class ResolutionException extends Exception {

    private final Map<String, SnapItem> resolutionErrors;

    public ResolutionException(final String message, final Map<String, SnapItem> validationErrors) {
        super(message);
        this.resolutionErrors = validationErrors;
    }

    public Map getValidationErrors() {
        return resolutionErrors;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        message.append("\nValidation Errors:\n");
        if (resolutionErrors != null) {
            for (Map.Entry<String, SnapItem> entry : resolutionErrors.entrySet()) {
                message.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        return message.toString();
    }
}