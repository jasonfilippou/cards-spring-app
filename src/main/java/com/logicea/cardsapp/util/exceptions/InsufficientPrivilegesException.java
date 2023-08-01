package com.logicea.cardsapp.util.exceptions;

import lombok.Getter;

/**
 * A {@link RuntimeException} thrown when a user attempts to perform an action for which they have insufficient
 * privileges (e.g accessing the cards of a different member user or an admin).
 *
 * @author jason
 */
@Getter
public class InsufficientPrivilegesException extends RuntimeException {

  private final String username;

  public InsufficientPrivilegesException(String username) {
    super("User " + username + " lacks privileges for this action.");
    this.username = username;
  }
}
