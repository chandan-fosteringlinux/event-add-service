package com.notification.Data;


import java.util.List;

public class ErrorResponse {
    private String status;
    private String message;
    private List<String> errors;

    // Constructor
    public ErrorResponse(String status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<String> getErrors() { return errors; }
}