package de.kel0002.salp.util;

public class MethodResult {
    boolean success;
    String message;

    public MethodResult() {
        success = true;
    }

    public MethodResult(String error) {
        success = false;
        message = error;
    }

    public boolean successful() {
        return success;
    }
    public boolean failed() {
        return !success;
    }

    public String error() {
        return message;
    }
}
