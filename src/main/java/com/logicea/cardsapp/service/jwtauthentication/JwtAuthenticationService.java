package com.logicea.cardsapp.service.jwtauthentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a single authentication method for users.
 *
 * @author jason
 * @see #authenticate(String, String)
 */
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

  private final AuthenticationManager authenticationManager;

  /**
   * Authenticates the &lt; username, password &gt; pair provided.
   *
   * @param username The user's username.
   * @param password The user's password.
   * @throws DisabledException if the authentication manager determines that the user is disabled.
   * @throws BadCredentialsException if the password provided did not correspond to the username
   *     provided.
   * @see AuthenticationManager
   * @see UsernamePasswordAuthenticationToken
   */
  public void authenticate(String username, String password)
      throws DisabledException, BadCredentialsException {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
  }
}
