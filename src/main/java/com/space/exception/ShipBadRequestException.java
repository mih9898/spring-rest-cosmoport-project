package com.space.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ShipBadRequestException  extends RuntimeException{
    public ShipBadRequestException(String message) {
        super(message);
    }

    public ShipBadRequestException() {
    }
}
