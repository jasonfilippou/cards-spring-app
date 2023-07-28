package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
@LoggedRepository
public class AggregateCardQueryRepositoryImpl implements AggregateCardQueryRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CardEntity> findCardsByProvidedFilters(AggregateGetQueryParams params) {
    // cr.orderBy(
    //  cb.asc(root.get("itemName")),
    //  cb.desc(root.get("itemPrice")));
    return entityManager
        .createQuery(criteriaQuery)
        .setMaxResults(params.getPageSize())
        .setFirstResult(params.getPage() * params.getPageSize())
        .getResultList();
    }
}
