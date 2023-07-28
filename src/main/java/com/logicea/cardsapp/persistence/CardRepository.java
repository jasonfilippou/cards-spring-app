package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@LoggedRepository
@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long>, AggregateCardQueryRepository {

}
