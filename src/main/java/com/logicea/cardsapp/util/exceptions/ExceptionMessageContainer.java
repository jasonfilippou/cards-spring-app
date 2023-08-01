package com.logicea.cardsapp.util.exceptions;

import lombok.AllArgsConstructor;

/**
 * A simple message container to allow exception handlers to be rendered by clients in JSON format.
 *
 * @author jason
 */
@AllArgsConstructor
public class ExceptionMessageContainer {
  public String message;
}
