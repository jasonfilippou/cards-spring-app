package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.util.logger.Logged;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A composed persistence repository used by the service layer to persist {@link CardEntity} instances.
 *
 * @see com.logicea.cardsapp.service.cards.CardService
 * @see AggregateCardQueryRepository
 * @see AggregateCardQueryRepositoryImpl
 *
 * @author jason
 */
@Repository
@Logged
public interface CardRepository
    extends JpaRepository<CardEntity, Long>, AggregateCardQueryRepository {}
