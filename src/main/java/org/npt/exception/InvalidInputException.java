package org.npt.exception;

import lombok.Getter;

import java.util.HashMap;


@Getter
public class InvalidInputException extends Exception {

    private final HashMap<String, String> errors;

    public InvalidInputException(String message, HashMap<String, String> errors) {
        super(message);
        this.errors = errors;
    }
}
