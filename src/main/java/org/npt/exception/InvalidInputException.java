package org.npt.exception;

import lombok.Getter;

import java.util.HashMap;


public class InvalidInputException extends Exception {

    @Getter
    private final HashMap<String, String> errors;

    public InvalidInputException(String message, HashMap<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}
