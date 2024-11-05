package org.npt.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GatewayNotFoundException extends Exception{

    public GatewayNotFoundException(String error){
        super(error);
    }
}
