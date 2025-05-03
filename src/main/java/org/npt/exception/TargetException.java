package org.npt.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TargetException extends Exception {

    public TargetException(String message) {
      super(message);
    }
}
