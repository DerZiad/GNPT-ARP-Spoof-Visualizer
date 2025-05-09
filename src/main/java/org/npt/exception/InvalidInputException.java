package org.npt.exception;

import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
public class InvalidInputException extends Exception {

    private HashMap<String, String> errors;

    public InvalidInputException(String message, HashMap<String,String> errors) {
        super(message);
        this.errors = errors;
    }
}
