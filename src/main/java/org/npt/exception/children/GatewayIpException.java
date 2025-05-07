package org.npt.exception.children;

import lombok.NoArgsConstructor;
import org.npt.exception.GatewayException;

@NoArgsConstructor
public class GatewayIpException extends GatewayException {

    public GatewayIpException(String message) {
        super(message);
    }
}
