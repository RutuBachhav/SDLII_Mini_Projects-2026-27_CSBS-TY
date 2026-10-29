package com.labloop.rest;

import java.util.List;

/** JSON body returned for errors, e.g. {"status":409,"error":"CONFLICT","messages":["..."]} */
public class ApiError {

    private int status;
    private String error;
    private List<String> messages;

    public ApiError() { }

    public ApiError(int status, String error, List<String> messages) {
        this.status = status;
        this.error = error;
        this.messages = messages;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }
}
