package org.npt.exception.children;

import org.npt.exception.GatewayException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GatewayIpException extends GatewayException {

    public GatewayIpException(String message) {
        super(message);
    }
}
