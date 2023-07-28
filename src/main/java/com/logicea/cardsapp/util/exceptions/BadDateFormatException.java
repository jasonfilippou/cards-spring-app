package com.logicea.cardsapp.util.exceptions;


/**
 * A {@link RuntimeException} thrown in cases of a bad date format provided by the user.
 *
 * @author jason
 */
public class BadDateFormatException extends RuntimeException{
    
    public BadDateFormatException(String msg){
        super(msg);
    }
}
