package org.npt.exception;

public class DrawNetworkException extends Exception {

    public DrawNetworkException(String message) {
        super(message);
    }

    public DrawNetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrawNetworkException(Throwable cause) {
        super(cause);
    }
}
