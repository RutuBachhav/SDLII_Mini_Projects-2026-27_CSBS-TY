package com.splitease.dto;

/** JSON body returned for every error: { "status": 400, "error": "..." } */
public class ErrorMessage {

    private int status;
    private String error;

    public ErrorMessage() { }

    public ErrorMessage(int status, String error) {
        this.status = status;
        this.error = error;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
