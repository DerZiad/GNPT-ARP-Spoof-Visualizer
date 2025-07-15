package org.npt.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ShutdownException extends Exception {

    public static final String ERROR_FORMAT = "%s : %s";

    private final Integer code;

    public ShutdownException(String message, ShutdownExceptionErrorCode code) {
        super(message);
        this.code = code.getCode();
    }

    @AllArgsConstructor
    @Getter
    public enum ShutdownExceptionErrorCode {

        FAILED_TO_LOAD_PROPERTY_FILE(10);

        private final Integer code;

    }
}
