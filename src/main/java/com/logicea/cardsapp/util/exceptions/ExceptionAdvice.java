package com.logicea.cardsapp.util.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * {@link RestControllerAdvice} for all our custom exceptions.
 * @author jason
 */
@RestControllerAdvice
public class ExceptionAdvice {

  /**
   * Handler for all exceptions that should return an HTTP Status Code of {@link HttpStatus#BAD_REQUEST}.
   * @param exc The {@link Exception} thrown by our application.
   * @return A {@link ResponseEntity} with the exception's message as the body and {@link HttpStatus#BAD_REQUEST} as the status code.
   */
  @ResponseBody
  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentNotValidException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ExceptionMessageContainer> badRequestStatusMessage(Exception exc) {
    return new ResponseEntity<>(new ExceptionMessageContainer(exc.getMessage()), HttpStatus.BAD_REQUEST);
  }

  /**
   * Handler for all exceptions that should return an HTTP Status Code of {@link HttpStatus#UNAUTHORIZED}.
   * @param exc The {@link Exception} thrown by our application.
   * @return A {@link ResponseEntity} with the exception's message as the body and {@link HttpStatus#UNAUTHORIZED} as the status code.
   */
  @ResponseBody
  @ExceptionHandler({
          BadCredentialsException.class
  })
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ExceptionMessageContainer> unauthorizedStatusMessage(Exception exc) {
    return new ResponseEntity<>(new ExceptionMessageContainer(exc.getMessage()), HttpStatus.UNAUTHORIZED);
  }

}