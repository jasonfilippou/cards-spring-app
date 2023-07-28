package com.logicea.cardsapp.util.exceptions;

import lombok.Getter;

@Getter
public class InsufficientPrivilegesException extends RuntimeException{

    private final String username;

    public InsufficientPrivilegesException(String username){
        super("User " + username + " lacks privileges for this action.");
        this.username = username;
    }
}
