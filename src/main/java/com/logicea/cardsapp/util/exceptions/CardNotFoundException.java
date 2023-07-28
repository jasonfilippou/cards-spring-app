package com.logicea.cardsapp.util.exceptions;

import lombok.Getter;

/**
 * A {@link RuntimeException} thrown when a card with a given ID cannot be found in the DB.
 * @author jason
 */
@Getter
public class CardNotFoundException extends RuntimeException {

    private final Long accountId;

    public CardNotFoundException(Long accountId) {
        super("Could not find card with id: " + accountId + ".");
        this.accountId = accountId;
    }
}