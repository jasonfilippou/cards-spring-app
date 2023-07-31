package com.logicea.cardsapp.util.exceptions;

/**
 * A {@link RuntimeException} thrown by PATCH requests when the user tries to update the name of a card
 * to the empty string. We do not allow that.
 *
 * @author jason
 */
public class CardNameCannotBeBlankException extends RuntimeException{

    public CardNameCannotBeBlankException(){
        super("Card name cannot be set to blank.");
    }
}
