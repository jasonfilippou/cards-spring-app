package com.logicea.cardsapp.service.cards;

import com.logicea.cardsapp.model.card.CardEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import static com.logicea.cardsapp.util.Constants.ADMIN_AUTHORITY;
import static com.logicea.cardsapp.util.Constants.MEMBER_AUTHORITY;

@Service
public class AccessCheckService {

    public boolean userIsMember(User user){
        return user.getAuthorities().contains(MEMBER_AUTHORITY) && user.getAuthorities().size() == 1;
    }
    public boolean userIsAdmin(User user){
        return user.getAuthorities().contains(ADMIN_AUTHORITY) && user.getAuthorities().size() == 1;
    }

    public boolean userHasAccessToCard(User user, CardEntity card){
        return userIsAdmin(user) || user.getUsername().equals(card.getCreatedBy());
    }
}
