package com.logicea.cardsapp.util;

import com.logicea.cardsapp.model.card.CardEntity;
import org.springframework.security.core.userdetails.User;

import static com.logicea.cardsapp.util.Constants.ADMIN_AUTHORITY;
import static com.logicea.cardsapp.util.Constants.MEMBER_AUTHORITY;

/**
 * A class with exclusively {@literal static} methods to help us is app development and minimize code duplication.
 */
public final class Utilities {

    private Utilities(){}

    public static boolean userIsMember(User user){
        return user.getAuthorities().contains(MEMBER_AUTHORITY) && user.getAuthorities().size() == 1;
    }
    public static boolean userIsAdmin(User user){
        return user.getAuthorities().contains(ADMIN_AUTHORITY) && user.getAuthorities().size() == 1;
    }

    public static boolean userHasAccessToCard(User user, CardEntity card){
        return userIsAdmin(user) || user.getUsername().equals(card.getCreatedBy());
    }
}
