package org.npt.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileException extends Exception {

    public FileException(String error) {
        super(error);
    }
}
