package org.npt.exception.children;

import lombok.NoArgsConstructor;
import org.npt.exception.TargetException;

@NoArgsConstructor
public class TargetIpException extends TargetException {

    public TargetIpException(String message) {
        super(message);
    }
}
