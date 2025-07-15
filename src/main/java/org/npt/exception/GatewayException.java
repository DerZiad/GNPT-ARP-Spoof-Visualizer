package org.npt.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GatewayException extends Exception {

    public GatewayException(String error) {
        super(error);
    }
}
