package org.npt.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProcessFailureException extends Exception {

    public ProcessFailureException(String error) {
        super(error);
    }
}
