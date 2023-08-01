package com.logicea.cardsapp.util;

import static com.logicea.cardsapp.util.Constants.*;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.model.user.UserDto;
import com.logicea.cardsapp.model.user.UserRole;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;

/**
 * Various constants and helper methods for our tests.
 * 
 * @author jason 
 */
public final class TestUtils {

  private TestUtils() {}
  public static final String ADMIN_EMAIL = "admin@company.com";
  public static final String ADMIN_PASSWORD = "adminpassword";
  public static final String MEMBER_ONE_EMAIL = "member1@company.com";
  public static final String MEMBER_ONE_PASSWORD = "member1password";

  public static final String MEMBER_TWO_EMAIL = "member2@company.com";
  public static final String MEMBER_TWO_PASSWORD = "member2password";

  public static final UserDto ADMIN_DTO = new UserDto(ADMIN_EMAIL, ADMIN_PASSWORD, UserRole.ADMIN);
  public static final UserDto MEMBER_ONE_DTO = new UserDto(MEMBER_ONE_EMAIL, MEMBER_ONE_PASSWORD, UserRole.MEMBER);

  public static final UserDto MEMBER_TWO_DTO = new UserDto(MEMBER_TWO_EMAIL, MEMBER_TWO_PASSWORD, UserRole.MEMBER);

  public static final User ADMIN_USER =
          new User(ADMIN_EMAIL, ADMIN_PASSWORD, Collections.singletonList(ADMIN_AUTHORITY));
  public static final User MEMBER_ONE_USER =
          new User(MEMBER_ONE_EMAIL, MEMBER_ONE_PASSWORD, Collections.singletonList(MEMBER_AUTHORITY));

  public static final User MEMBER_TWO_USER =
          new User(MEMBER_TWO_EMAIL, MEMBER_TWO_PASSWORD, Collections.singletonList(MEMBER_AUTHORITY));

  public static final UsernamePasswordAuthenticationToken ADMIN_UPAT =
          new UsernamePasswordAuthenticationToken(ADMIN_USER, null, ADMIN_USER.getAuthorities());
  public static final UsernamePasswordAuthenticationToken MEMBER_ONE_UPAT =
          new UsernamePasswordAuthenticationToken(MEMBER_ONE_USER, null, MEMBER_ONE_USER.getAuthorities());

  public static final UsernamePasswordAuthenticationToken MEMBER_TWO_UPAT =
          new UsernamePasswordAuthenticationToken(MEMBER_TWO_USER, null, MEMBER_TWO_USER.getAuthorities());

  // We will give our DTOs IDs and creation dates because it will make some tests easier to write.
  private static Long currentCardId = 0L;
  
  // Get n DTOs of status IN_PROGRESS.
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
                        .createdDateTime(LocalDateTime.now().minusDays(2L))
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
                        .createdDateTime(LocalDateTime.now().minusDays(2L))
                        .build()));
    return retVal;
  }

  // Get n DTOs of color #45F780

  public static final String COLOR = "#45F780";

  public static List<CardDto> getNDtosOfSpecificColor(int n) {
    List<CardDto> retVal = Lists.newArrayListWithCapacity(n);
    IntStream.range(0, n)
        .forEach(
            i ->
                retVal.add(
                    CardDto.builder()
                        .id(++currentCardId)
                        .name((i < n / 3) ? "CARD #" + currentCardId : NAME) // To add some variability in the queries.
                        .description("Description of card: " + currentCardId)
                        .color(COLOR)
                        .status(CardStatus.DONE)
                        .createdDateTime(LocalDateTime.now().minusDays(2L))
                        .build()));
    return retVal;
  }
  // Get n DTOs that were created in a decreasing number of minutes before New Year's Eve 2023.
  
  public static List<CardDto> getNDtosOfIncreasingCreatedTime(int n) {
    List<CardDto> retVal = Lists.newArrayListWithCapacity(n);
    IntStream.range(0, n)
        .forEach(
            i ->
                retVal.add(
                    CardDto.builder()
                        .id(++currentCardId)
                        .name("CARD #" + currentCardId)
                        .description("Description of card: " + currentCardId)
                        .color("#4F123O")
                        .status((i < n / 2) ? CardStatus.DONE : STATUS) // To add some variability in the queries.
                        .createdDateTime(LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)
                                .minusMinutes(n - i))
                        .build()));
    return retVal;
  }

  // Concatenate 20 of those DTO lists into one big list.
  public static final List<CardDto> CARD_DTOS =
      ImmutableList.copyOf(
          Iterables.concat(
              getNInProgressDtos(5),
              getNDtosOfSpecificColor(5),
              getNDtosOfIncreasingCreatedTime(5),
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
    retVal.setCreatedDateTime(cardDto.getCreatedDateTime());
    retVal.setCreatedBy(MEMBER_ONE_EMAIL);
    retVal.setLastModifiedDateTime(LocalDateTime.now());
    retVal.setLastModifiedBy(ADMIN_EMAIL);
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
            compareFieldsInGivenOrder(p1, p2, sortByField, sortOrder));
  }

  public static <T extends Comparable<T>> int compareFieldsInGivenOrder(
      Object pojoOne, Object pojoTwo, String sortByField, SortOrder sortOrder) {
    try {
      assert pojoOne.getClass().equals(pojoTwo.getClass());
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(sortByField, pojoOne.getClass());
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
          "Field " + sortByField + " of " + pojoOne.getClass().getSimpleName() + " is not Comparable.");
    }
  }
  
  /* Other utilities */

  // We will consider a CardDto to be "equal" to a CardEntity if the non-audit fields match.
  public static boolean cardDtoAndEntityEqual(@NonNull CardDto cardDto, @NonNull CardEntity cardEntity) {
    return Objects.equals(cardDto.getId(), cardEntity.getId())
            && Objects.equals(cardDto.getStatus(), cardEntity.getStatus())
            && Objects.equals(cardDto.getDescription(), cardEntity.getDescription())
            && Objects.equals(cardDto.getColor(), cardEntity.getColor())
            && Objects.equals(cardDto.getName(), cardEntity.getName());
  }

  // We will do the same for two CardDto instances.
  public static boolean cardDtosEqual(@NonNull CardDto dtoOne, @NonNull CardDto dtoTwo) {
    return Objects.equals(dtoOne.getId(), dtoTwo.getId())
            && Objects.equals(dtoOne.getStatus(), dtoTwo.getStatus())
            && Objects.equals(dtoOne.getDescription(), dtoTwo.getDescription())
            && Objects.equals(dtoOne.getColor(), dtoTwo.getColor())
            && Objects.equals(dtoOne.getName(), dtoTwo.getName());
  }
}
