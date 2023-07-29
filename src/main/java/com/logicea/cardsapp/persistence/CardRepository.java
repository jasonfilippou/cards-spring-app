package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.util.logger.Logged;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Logged
public interface CardRepository
    extends JpaRepository<CardEntity, Long>, AggregateCardQueryRepository {}
