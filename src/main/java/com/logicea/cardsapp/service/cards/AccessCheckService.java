package com.logicea.cardsapp.service.cards;

import static com.logicea.cardsapp.util.Constants.ADMIN_AUTHORITY;
import static com.logicea.cardsapp.util.Constants.MEMBER_AUTHORITY;

import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.user.UserRole;
import com.logicea.cardsapp.util.logger.Logged;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

/**
 * A service that provides methods for checking if a specified {@link User} has specific privileges.
 * 
 * @author jason 
 */
@Service
@Logged
public class AccessCheckService {

  /**
   * Checks to see if a {@link User} has ONLY the authorities of a {@link UserRole#MEMBER}.
   * @param user A {@link User} instance.
   * @return {@literal true} iff the {@link User} has ONLY the authorities of a {@link UserRole#MEMBER}.
   */
  public boolean userIsMember(User user) {
    return user.getAuthorities().contains(MEMBER_AUTHORITY) && user.getAuthorities().size() == 1;
  }

  /**
   * Checks to see if a {@link User} has ONLY the authorities of a {@link UserRole#ADMIN}.
   * @param user A {@link User} instance.
   * @return {@literal true} iff the {@link User} has ONLY the authorities of a {@link UserRole#ADMIN}.
   */
  public boolean userIsAdmin(User user) {
    return user.getAuthorities().contains(ADMIN_AUTHORITY) && user.getAuthorities().size() == 1;
  }

  /**
   * Checks to see if a {@link User} has access to a specific card.
   * @param user A {@link User} instance.
   * @param card A {@link CardEntity} instance pulled from our database.
   * @return {@literal true} iff the {@link User} has access to the card.
   */
  public boolean userHasAccessToCard(User user, CardEntity card) {
    return userIsAdmin(user) || user.getUsername().equals(card.getCreatedBy());
  }
}
