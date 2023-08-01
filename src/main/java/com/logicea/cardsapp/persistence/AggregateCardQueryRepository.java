package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.logger.Logged;
import java.util.List;
import org.springframework.security.core.userdetails.User;

/**
 * A persistence interface used exclusively to provide a composable interface pattern. 
 * 
 * @author jason 
 * 
 * @see AggregateCardQueryRepositoryImpl
 */
@Logged
public interface AggregateCardQueryRepository {

  /**
   * Returns a paginated and sorted list of {@link CardEntity} instances according to the specified criteria.
   * @param params An instance of {@link AggregateGetQueryParams} that contains all the information for pagination
   *               and sorting.
   * @param loggedInUser The currently logged in {@link User}.
   *                     
   * @return A list of {@link CardEntity} instances that satisfy the provided criteria.
   */
  List<CardEntity> findCardsByProvidedFilters(AggregateGetQueryParams params, User loggedInUser);
}
