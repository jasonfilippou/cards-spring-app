package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

@LoggedRepository
public interface CardRepository extends JpaRepository<CardEntity, Long> {}
