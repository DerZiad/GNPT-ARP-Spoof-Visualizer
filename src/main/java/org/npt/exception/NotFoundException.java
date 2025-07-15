package org.npt.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotFoundException extends Exception {

    public NotFoundException(String error) {
        super(error);
    }
}
