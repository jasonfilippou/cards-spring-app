package com.logicea.cardsapp.service.jwtauthentication;

import static com.logicea.cardsapp.util.Constants.ADMIN_AUTHORITY;
import static com.logicea.cardsapp.util.Constants.MEMBER_AUTHORITY;

import com.logicea.cardsapp.controller.JwtAuthenticationController;
import com.logicea.cardsapp.model.user.UserDto;
import com.logicea.cardsapp.model.user.UserEntity;
import com.logicea.cardsapp.model.user.UserRole;
import com.logicea.cardsapp.persistence.UserRepository;
import com.logicea.cardsapp.util.JwtRequestFilter;
import com.logicea.cardsapp.util.exceptions.EmailAlreadyInDatabaseException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class that talks to the database to retrieve and store user information.
 *
 * @author jason
 * @see JwtRequestFilter
 * @see JwtAuthenticationService
 * @see JwtAuthenticationController
 */
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder encoder;

  /**
   * Load a user from the database given their email. The name of the method is confusing because
   * that's how {@link UserDetailsService} names it; don't shoot the messenger here. For our
   * application, the username is the e-mail.
   *
   * @param email the user's e-mail.
   * @return An instance of {@link org.springframework.security.core.userdetails.User} that contains
   *     the user's username, password and authorities.
   * @throws UsernameNotFoundException if no user with username {@literal username} exists in the
   *     database.
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<UserEntity> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      // Return an appropriate instance of org.springframework.security.core.userdetails.User
      return new org.springframework.security.core.userdetails.User(
          user.get().getEmail(),
          user.get().getPassword(),
          Collections.singletonList(
              user.get().getRole() == UserRole.ADMIN ? ADMIN_AUTHORITY : MEMBER_AUTHORITY));
    } else {
      throw new UsernameNotFoundException("User with email: " + email + " not found.");
    }
  }

  /**
   * Save a new user in the database.
   *
   * @param newUser A {@link UserDto} with the information of the new user to store in the database.
   * @return A {@link UserDto} corresponding to the just persisted user.
   * @throws EmailAlreadyInDatabaseException If the username provided already exists in the
   *     database.
   */
  public UserDto save(UserDto newUser) throws EmailAlreadyInDatabaseException {
    try {
      UserEntity savedUser =
          userRepository.save(
              new UserEntity(
                  newUser.getEmail().trim(),
                  encoder.encode(newUser.getPassword()),
                  newUser.getRole()));
      return new UserDto(savedUser.getEmail(), savedUser.getPassword(), savedUser.getRole());
    } catch (DataIntegrityViolationException integrityViolationException) {
      throw new EmailAlreadyInDatabaseException(newUser.getEmail().trim());
    }
  }
}
