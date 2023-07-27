package com.logicea.cardsapp.model.jwt;

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Simple POJO that defines the API's response to the user for a JWT token request.
 *
 * @author jason
 *
 * @see JwtRequest
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class JwtResponse implements Serializable {

  @Serial private static final long serialVersionUID = -8091879091924046844L;
  @ToString.Exclude private final String jwtToken;
}
