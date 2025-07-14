package org.npt.exception.children;

import org.npt.exception.GatewayException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GatewayNotFoundException extends GatewayException {

    public GatewayNotFoundException(String error) {
        super(error);
    }
}
