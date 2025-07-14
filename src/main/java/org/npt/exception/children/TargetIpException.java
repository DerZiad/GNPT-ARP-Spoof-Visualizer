package org.npt.exception.children;

import org.npt.exception.TargetException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TargetIpException extends TargetException {

    public TargetIpException(String message) {
        super(message);
    }
}
