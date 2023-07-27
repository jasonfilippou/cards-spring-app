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
 *
 * @see #authenticate(String, String)
 */
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

  private final AuthenticationManager authenticationManager;

  /**
   * Authenticates the &lt; username, password &gt; pair provided.
   * @param username The user's username.
   * @param password The user's password.
   * @throws Exception if the underlying {@link AuthenticationManager} throws a {@link DisabledException} or {@link BadCredentialsException}.
   * @see AuthenticationManager
   * @see UsernamePasswordAuthenticationToken
   */
  public void authenticate(String username, String password) throws Exception {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));
    } catch (DisabledException e) {
      throw new Exception("USER_DISABLED", e);
    } catch (BadCredentialsException e) {
      throw new Exception("INVALID_CREDENTIALS", e);
    }
  }
}
