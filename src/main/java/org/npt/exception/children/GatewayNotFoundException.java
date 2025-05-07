package org.npt.exception.children;

import lombok.NoArgsConstructor;
import org.npt.exception.GatewayException;

@NoArgsConstructor
public class GatewayNotFoundException extends GatewayException {

    public GatewayNotFoundException(String error){
        super(error);
    }
}
