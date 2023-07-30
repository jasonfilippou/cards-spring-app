package com.logicea.cardsapp.util;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;

public final class TestUtils {

  private TestUtils() {}

  public static final Random RANDOM = new Random(47);
  public static final String ADMIN = "ADMIN";
  public static final String MEMBER = "MEMBER";

  // We will give our DTOs IDs because it will make some tests easier to write.
  private static Long currentCardId = 0L;

  public static String randomColorHex() {
    return "#" + RandomStringUtils.randomAlphanumeric(6);
  }

  // Get 5 DTOs of status IN_PROGRESS.

  public static final CardStatus STATUS = CardStatus.IN_PROGRESS;

  public static List<CardDto> getNInProgressDtos(int n) {
    List<CardDto> retVal = Lists.newArrayListWithCapacity(n);
    IntStream.range(0, n)
        .forEach(
            _i ->
                retVal.add(
                    CardDto.builder()
                        .id(++currentCardId)
                        .name("CARD #" + currentCardId)
                        .description("Description of card: " + currentCardId)
                        .color("#45F281")
                        .status(STATUS)
                        .build()));
    return retVal;
  }

  // Get n DTOs with name "MEME_CARD"

  public static final String NAME = "MEME_CARD";

  public static List<CardDto> getNMemeCardNameDtos(int n) {
    List<CardDto> retVal = Lists.newArrayListWithCapacity(n);
    IntStream.range(0, n)
        .forEach(
            _i ->
                retVal.add(
                    CardDto.builder()
                        .id(++currentCardId)
                        .name(NAME)
                        .description("Description of card: " + currentCardId)
                        .color("#4F141B")
                        .status(CardStatus.TODO)
                        .build()));
    return retVal;
  }

  // Get n DTOs of color #45F780

  public static final String COLOR = "#45F780";

  public static List<CardDto> getNDtosOfSpecificColor(int n) {
    List<CardDto> retVal = Lists.newArrayListWithCapacity(n);
    IntStream.range(0, n)
        .forEach(
            _i ->
                retVal.add(
                    CardDto.builder()
                        .id(++currentCardId)
                        .name("CARD #" + currentCardId)
                        .description("Description of card: " + currentCardId)
                        .color(COLOR)
                        .status(CardStatus.DONE)
                        .build()));
    return retVal;
  }
  // Get n DTOs of description "desc"

  public static final String DESCRIPTION = "desc";

  public static List<CardDto> getNDtosOfDescriptionDesc(int n) {
    List<CardDto> retVal = Lists.newArrayListWithCapacity(n);
    IntStream.range(0, n)
        .forEach(
            _i ->
                retVal.add(
                    CardDto.builder()
                        .id(++currentCardId)
                        .name("CARD #" + currentCardId)
                        .description(DESCRIPTION)
                        .color(COLOR)
                        .status(CardStatus.DONE)
                        .build()));
    return retVal;
  }

  // Concatenate 20 of those DTO lists into one big list.
  public static final List<CardDto> CARD_DTOS =
      ImmutableList.copyOf(
          Iterables.concat(
              getNInProgressDtos(5),
              getNDtosOfSpecificColor(5),
              getNDtosOfDescriptionDesc(5),
              getNMemeCardNameDtos(5)));

  // Make a list of relevant entities.

  public static CardEntity fromCardDtoToCardEntity(CardDto cardDto) {
    CardEntity retVal = CardEntity.builder()
            .id(cardDto.getId())
            .name(cardDto.getName())
            .description(cardDto.getDescription())
            .status(cardDto.getStatus())
            .color(cardDto.getColor())
            .build();
    // And now let's create some standard audit fields
    retVal.setCreatedDateTime(LocalDateTime.now().minusDays(2L));
    retVal.setCreatedBy(MEMBER);
    retVal.setLastModifiedDateTime(LocalDateTime.now().minusDays(1L));
    retVal.setLastModifiedBy(ADMIN);
    return retVal;
  }

  public static final List<CardEntity> CARD_ENTITIES =
      CARD_DTOS.stream().map(TestUtils::fromCardDtoToCardEntity).collect(Collectors.toList());

  /* Some utilities for comparing fields of arbitrary POJOs */

  public static boolean collectionIsSortedByFieldInGivenDirection(
      Collection<?> pojos, String sortByField, SortOrder sortOrder) {
    // Using a Guava dependency here
    return Comparators.isInOrder(
        pojos,
        (p1, p2) ->
            compareFieldsInGivenOrder(p1.getClass(), p2.getClass(), sortByField, sortOrder));
  }

  public static <T extends Comparable<T>> int compareFieldsInGivenOrder(
      Class<?> pojoOne, Class<?> pojoTwo, String sortByField, SortOrder sortOrder) {
    try {
      assert pojoOne.equals(pojoTwo);
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(sortByField, pojoOne);
      Method appropriateGetter = propertyDescriptor.getReadMethod();
      @SuppressWarnings("unchecked")
      T pojoOneFieldValue = (T) appropriateGetter.invoke(pojoOne);
      @SuppressWarnings("unchecked")
      T pojoTwoFieldValue = (T) appropriateGetter.invoke(pojoTwo);
      return sortOrder == SortOrder.ASC
          ? pojoOneFieldValue.compareTo(pojoTwoFieldValue)
          : pojoTwoFieldValue.compareTo(pojoOneFieldValue);
    } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    } catch (ClassCastException exc) {
      throw new RuntimeException(
          "Field " + sortByField + " of " + pojoOne.getSimpleName() + " is not Comparable.");
    }
  }
}
