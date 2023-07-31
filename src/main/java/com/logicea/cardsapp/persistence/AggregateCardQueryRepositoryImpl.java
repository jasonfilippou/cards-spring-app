package com.logicea.cardsapp.persistence;

import static com.logicea.cardsapp.util.Constants.*;

import com.google.common.collect.Lists;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.service.cards.AccessCheckService;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.SortOrder;
import com.logicea.cardsapp.util.exceptions.BadDateFormatException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AggregateCardQueryRepositoryImpl implements AggregateCardQueryRepository {

  private final AccessCheckService accessCheckService;
  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<CardEntity> findCardsByProvidedFilters(
      AggregateGetQueryParams params, User loggedInUser) throws BadDateFormatException {
    // Create the CriteriaBuilder, CriteriaQuery and Root<> instances.
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<CardEntity> criteriaQuery = criteriaBuilder.createQuery(CardEntity.class);
    Root<CardEntity> cardRoot = criteriaQuery.from(CardEntity.class);

    // Set the predicates the conjunction of which will make up the WHERE clause.
    List<Predicate> predicates;
    try {
      predicates =
          extractPredicatesFromFilterParams(
              params.getFilterParams(), criteriaBuilder, cardRoot, loggedInUser);
    } catch (ParseException pe) {
      throw new BadDateFormatException(pe.getMessage());
    }
    criteriaQuery.where(predicates.toArray(new Predicate[0]));

    // Sort the results by the desired field in the desired order.
    criteriaQuery.orderBy(
        params.getSortOrder() == SortOrder.ASC
            ? criteriaBuilder.asc(cardRoot.get(params.getSortByField()))
            : criteriaBuilder.desc(
                cardRoot.get(
                    params
                        .getSortByField()))); // We could also do second, third, ... n - level
                                              // sorts.

    // Return a paginated form of the results. TODO: could we cache the previous or next page to
    // speed things up?
    return entityManager
        .createQuery(criteriaQuery)
        .setMaxResults(params.getPageSize())
        .setFirstResult(params.getPage() * params.getPageSize())
        .getResultList();
  }

  // Not easily maintainable code; we could use the Specification interface to generify our
  // predicates. TODO: improve the following using the Specification interface.
  private List<Predicate> extractPredicatesFromFilterParams(
      Map<String, String> params, CriteriaBuilder cb, Root<CardEntity> root, User user)
      throws ParseException {
    // Have to be careful to use the exact names of the fields in CardDto.
    List<Predicate> retVal = Lists.newArrayList();
    if (params.containsKey(NAME_FILTER_STRING)) {
      retVal.add(cb.equal(root.get("name"), params.get(NAME_FILTER_STRING)));
    }
    if (params.containsKey(COLOR_FILTER_STRING)) {
      retVal.add(cb.equal(root.get("color"), params.get(COLOR_FILTER_STRING)));
    }
    if (params.containsKey(STATUS_FILTER_STRING)) {
      retVal.add(
          cb.equal(root.get("status"), CardStatus.valueOf(params.get(STATUS_FILTER_STRING))));
    }
    if (params.containsKey(BEGIN_CREATION_DATE_FILTER_STRING)) {
      retVal.add(
          cb.greaterThanOrEqualTo(
              root.get("createdDateTime"),
              LocalDateTime.parse(
                  params.get(BEGIN_CREATION_DATE_FILTER_STRING), DATE_TIME_FORMATTER)));
    }
    if (params.containsKey(END_CREATION_DATE_FILTER_STRING)) {
      retVal.add(
          cb.lessThanOrEqualTo(
              root.get("createdDateTime"),
              LocalDateTime.parse(
                  params.get(END_CREATION_DATE_FILTER_STRING), DATE_TIME_FORMATTER)));
    }
    if (params.containsKey(CREATING_USER_FILTER_STRING)) {
      retVal.add(cb.equal(root.get("createdBy"), params.get(CREATING_USER_FILTER_STRING)));
    }
    // Don't forget that if the user isn't an admin, we can only allow them access
    // to cards they themselves have created.
    if (!accessCheckService.userIsAdmin(user)) {
      retVal.add(cb.equal(root.get("createdBy"), user.getUsername()));
    }
    return retVal;
  }
}
