package com.logicea.cardsapp.util.exceptions;


/**
 * A {@link RuntimeException} instance thrown in POST Card requests when the user has not provided a 
 * name for the card. 
 * 
 * @author jason 
 */
public class CardNameNotProvidedException extends RuntimeException{
    public CardNameNotProvidedException(){
        super("Please provide a name for the card.");
    }
}
