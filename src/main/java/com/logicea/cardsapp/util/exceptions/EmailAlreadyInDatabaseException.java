package com.logicea.cardsapp.util.exceptions;

import lombok.Getter;

/** A {@link RuntimeException} thrown when the user tries to register with a username that already exists
 * in the database.
 * @author jason
 */
@Getter
public class EmailAlreadyInDatabaseException extends RuntimeException {

    private final String username;

    public EmailAlreadyInDatabaseException(String username){
        super("Username " + username + " already in database.");
        this.username = username;
    }
}
