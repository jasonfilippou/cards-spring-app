package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import java.util.List;
import org.springframework.security.core.userdetails.User;

public interface AggregateCardQueryRepository {
  List<CardEntity> findCardsByProvidedFilters(AggregateGetQueryParams params, User loggedInUser);
}
