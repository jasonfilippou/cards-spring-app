package com.logicea.cardsapp.unit.service.cards;

import static com.logicea.cardsapp.util.Constants.*;
import static com.logicea.cardsapp.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.service.cards.AccessCheckService;
import com.logicea.cardsapp.service.cards.CardService;
import com.logicea.cardsapp.service.cards.PatchMapper;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.PaginationTester;
import com.logicea.cardsapp.util.SortOrder;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import com.logicea.cardsapp.util.exceptions.InsufficientPrivilegesException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@RunWith(MockitoJUnitRunner.class)
public class CardServiceUnitTests {

  @Mock private CardRepository cardRepository;

  @Mock private AccessCheckService accessCheckService;

  @Mock
  private PatchMapper patchMapper;

  @InjectMocks private CardService cardService;

  public static final String ADMIN_EMAIL = "admin@company.com";
  public static final String ADMIN_PASSWORD = "adminpassword";
  public static final String MEMBER_EMAIL = "member@company.com";
  public static final String MEMBER_PASSWORD = "memberpassword";
  private static final User ADMIN_USER =
      new User(ADMIN_EMAIL, ADMIN_PASSWORD, Collections.singletonList(ADMIN_AUTHORITY));
  private static final User MEMBER_USER =
      new User(MEMBER_EMAIL, MEMBER_PASSWORD, Collections.singletonList(MEMBER_AUTHORITY));

  private static final UsernamePasswordAuthenticationToken ADMIN_UPAT =
      new UsernamePasswordAuthenticationToken(ADMIN_USER, null, ADMIN_USER.getAuthorities());
  private static final UsernamePasswordAuthenticationToken MEMBER_UPAT =
      new UsernamePasswordAuthenticationToken(MEMBER_USER, null, MEMBER_USER.getAuthorities());
  private static final CardDto CARD_DTO =
      CardDto.builder()
          .id(1L)
          .name("CARD-1")
          .description("A test card with ID 1")
          .status(CardStatus.TODO)
          .color("#G78JK0")
          .build();

  private static final CardEntity CARD_ENTITY =
      CardEntity.builder()
          .id(CARD_DTO.getId())
          .name(CARD_DTO.getName())
          .description(CARD_DTO.getDescription())
          .status(CARD_DTO.getStatus())
          .color(CARD_DTO.getColor())
          .build();

  // We will consider a CardDto to be "equal" to a CardEntity if the non-audit fields match.
  private static boolean cardDtoAndEntityEqual(CardDto cardDto, CardEntity cardEntity) {
    return Objects.equals(cardDto.getId(), cardEntity.getId())
        && Objects.equals(cardDto.getStatus(), cardEntity.getStatus())
        && Objects.equals(cardDto.getDescription(), cardEntity.getDescription())
        && Objects.equals(cardDto.getColor(), cardEntity.getColor())
        && Objects.equals(cardDto.getName(), cardEntity.getName());
  }

  // We will do the same for two CardDto instances.
  private static boolean cardDtosEqual(CardDto dtoOne, CardDto dtoTwo) {
    return Objects.equals(dtoOne.getId(), dtoTwo.getId())
        && Objects.equals(dtoOne.getStatus(), dtoTwo.getStatus())
        && Objects.equals(dtoOne.getDescription(), dtoTwo.getDescription())
        && Objects.equals(dtoOne.getColor(), dtoTwo.getColor())
        && Objects.equals(dtoOne.getName(), dtoTwo.getName());
  }

  /* GET by ID tests */

  @Test
  public void whenRepoGetsAnEntityByIdSuccessfully_thenRelevantDtoIsReturned() {
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    when(accessCheckService.userHasAccessToCard(ADMIN_USER, CARD_ENTITY)).thenReturn(true);
    assertTrue(cardDtoAndEntityEqual(cardService.getCard(CARD_DTO.getId()), CARD_ENTITY));
  }

  @Test(expected = CardNotFoundException.class)
  public void whenRepoCannotFindTheCard_thenCardNotFoundExceptionIsThrown() {
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.empty());
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    cardService.getCard(CARD_DTO.getId());
  }

  @Test(expected = InsufficientPrivilegesException.class)
  public void
      whenUserHasInsufficientPrivilegesToGetCard_thenInsufficientPrivilegesExceptionIsThrown() {
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    when(accessCheckService.userHasAccessToCard(MEMBER_USER, CARD_ENTITY)).thenReturn(false);
    cardService.getCard(CARD_DTO.getId());
  }

  /* Aggregate GET tests */

  @Test
  public void whenRepoReturnsASortedPage_andNoFiltersAreUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 20 Cards total.

    // First, test a page with 20 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(1)
        .pageSize(20)
        .pojoType(CardDto.class)
        .expectedPageSizes(Collections.singletonList(20))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Second, test 2 pages of 10 cards each.
    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(2)
        .pageSize(10)
        .pojoType(CardDto.class)
        .expectedPageSizes(List.of(10, 10))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Third, test 4 pages of 5 cards each.
    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(4)
        .pageSize(5)
        .pojoType(CardDto.class)
        .expectedPageSizes(List.of(5, 5, 5, 5))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Fourth and final, test 7 pages with 3, 3, 3, 3, 3, 3 and 2 cards respectively.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(7)
        .pageSize(3)
        .pojoType(CardDto.class)
        .expectedPageSizes(List.of(3, 3, 3, 3, 3, 3, 2))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);
  }

  @SuppressWarnings("unchecked")
  private void testPaginatedAndSortedAggregateGet(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    Predicate<CardEntity> filter = Optional.ofNullable((Predicate<CardEntity>) params.getPredicate()).orElse(cardEntity -> true);
    List<CardEntity> sortedList =
        CARD_ENTITIES.stream()
            .filter(filter)
            .sorted((t1, t2) -> compareFieldsInGivenOrder(t1, t2, sortByField, sortOrder))
            .toList();
    List<CardEntity> slicedList =
        sortedList.subList(
                page * pageSize,
                Math.min(pageSize * page + pageSize, sortedList.size()));
    mockWithExpectedList(params, slicedList);
    makePaginationAndSortingQueryAssertions(
        cardService.getAllCardsByFilter(params), expectedNumEntries, sortByField, sortOrder);
  }

  private void mockWithExpectedList(AggregateGetQueryParams params, List<CardEntity> expectedList) {
    when(cardRepository.findCardsByProvidedFilters(params, ADMIN_USER)).thenReturn(expectedList);
    // Mocking the accessCheckService just for consistency, we've logged in as ADMINs anyhow for
    // these tests.
    when(accessCheckService.userIsMember(ADMIN_USER)).thenReturn(false);
  }

  private void makePaginationAndSortingQueryAssertions(
      List<CardDto> returnedDtos, int expectedNumEntries, String sortByField, SortOrder sortOrder) {
    assertEquals(expectedNumEntries, returnedDtos.size());
    assertTrue(collectionIsSortedByFieldInGivenDirection(returnedDtos, sortByField, sortOrder));
  }

  @Test
  public void whenRepoReturnsASortedPage_andNameFilterIsUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 9 cards that share the same name.

    // First, test a page with 9 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(1)
        .pageSize(9)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity->cardEntity.getName().equals(NAME))
        .expectedPageSizes(List.of(9))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Second, test 3 pages with 3 cards each.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(3)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity->cardEntity.getName().equals(NAME))
        .expectedPageSizes(List.of(3, 3, 3))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Third and final, test 3 pages, with 4 + 4 + 1 cards each, respectively.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(3)
        .pageSize(4)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity->cardEntity.getName().equals(NAME))
        .expectedPageSizes(List.of(4, 4, 1))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);
  }

  @Test
  public void whenRepoReturnsASortedPage_andColorFilterIsUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 5 cards that share the same color.

    // First, test a page with 5 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(1)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> cardEntity.getColor().equals(COLOR))
        .expectedPageSizes(Collections.singletonList(5))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Second, test 2 pages, one with 3 and another one with 2 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(2)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> cardEntity.getColor().equals(COLOR))
        .expectedPageSizes(List.of(3, 2))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Third and final, test 3 pages, with 2 + 2 + 1 cards each, respectively.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(3)
        .pageSize(2)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> cardEntity.getColor().equals(COLOR))
        .expectedPageSizes(List.of(2, 2, 1))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);
  }

  @Test
  public void whenRepoReturnsASortedPage_andStatusFilterIsUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 8 cards that share the same status.

    // First, test a page with 8 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(1)
        .pageSize(8)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> cardEntity.getStatus().equals(STATUS))
        .expectedPageSizes(Collections.singletonList(8))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Second, test 2 pages, one with 5 and another one with 3 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(2)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> cardEntity.getStatus().equals(STATUS))
        .expectedPageSizes(List.of(5, 3))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);
  }

  @Test
  public void whenRepoReturnsASortedPage_andDateOfCreationFiltersAreUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 5 cards created in the set {now() - 5min, now() - 4min, ... now() - 1min}.

    // First, test a page with 5 cards.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(1)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> creationDateBetween(cardEntity.getCreatedDateTime(),
                LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)
                        .minusMinutes(5),  LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)))
        .expectedPageSizes(Collections.singletonList(5))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Second, test 2 pages, one with 4 and another one with 1 card.

    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(2)
        .pageSize(4)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> creationDateBetween(cardEntity.getCreatedDateTime(),
                  LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)
                      .minusMinutes(5),  LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)))
        .expectedPageSizes(List.of(4, 1))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);
  }

  private boolean creationDateBetween(
      LocalDateTime creationDateTime,
      LocalDateTime fromDateTimeIncl,
      LocalDateTime toDateTimeIncl) {
    assert !toDateTimeIncl.isBefore(fromDateTimeIncl);
    return creationDateTime.isEqual(fromDateTimeIncl)
        || creationDateTime.isEqual(toDateTimeIncl)
        || creationDateTime.isAfter(fromDateTimeIncl) && creationDateTime.isBefore(toDateTimeIncl);
  }

  @Test
  public void
      whenRepoReturnsASortedPage_andStatusAndDateOfCreationFiltersAreUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 3 cards created within the last 5 minutes with a status of IN_PROGRESS.

    // First, test a page of all 3 cards.
    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(1)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filteringPredicate(cardEntity -> creationDateBetween(cardEntity.getCreatedDateTime(), 
                LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)
                .minusMinutes(5), LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)) && 
                cardEntity.getStatus().equals(STATUS))
        .expectedPageSizes(Collections.singletonList(3))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);

    // Second, test 2 pages, 1 with 2 and 1 with 1 card.
    PaginationTester.<CardDto, CardEntity>builder()
        .totalPages(2)
        .pageSize(2)
        .pojoType(CardDto.class)
            .filteringPredicate(cardEntity -> creationDateBetween(cardEntity.getCreatedDateTime(),
                    LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)
                            .minusMinutes(5), LocalDateTime.parse("01/01/2023 00:00:00.000", DATE_TIME_FORMATTER)) &&
                    cardEntity.getStatus().equals(STATUS))
        .expectedPageSizes(List.of(2, 1))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGet);
  }

  @Test(expected = InsufficientPrivilegesException.class)
  public void
      whenAttemptingToGetADifferentUsersCards_whileNotAdmin_thenInsufficientPrivilegesExceptionIsThrown() {
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    AggregateGetQueryParams paramsWithOtherUsersEmail =
        AggregateGetQueryParams.builder()
            .filterParams(Map.of(CREATING_USER_FILTER_STRING, ADMIN_EMAIL))
            .build();
    // Mocking just for consistency, since this would return true anyhow.
    when(accessCheckService.userIsMember(MEMBER_USER)).thenReturn(true);
    cardService.getAllCardsByFilter(paramsWithOtherUsersEmail);
  }

  /* POST tests */

  @Test
  public void whenRepoPersistsACardSuccessfully_thenTheCardIsReturned() {
    when(cardRepository.save(any(CardEntity.class))).thenReturn(CARD_ENTITY);
    assertTrue(cardDtosEqual(CARD_DTO, cardService.storeCard(CARD_DTO)));
  }

  /* DELETE tests */

  @Test
  public void whenRepoFindsTheCardById_andUserHasAccessToIt_thenDeleteShouldWork() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(ADMIN_USER, CARD_ENTITY)).thenReturn(true);
    doNothing().when(cardRepository).deleteById(CARD_DTO.getId());
    cardService.deleteCard(CARD_DTO.getId());
  }

  @Test(expected = InsufficientPrivilegesException.class)
  public void
      whenRepoFindsTheCardById_andUserDoesNotHaveAccessToIt_thenInsufficientPrivilegesExceptionIsThrown() {
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(MEMBER_USER, CARD_ENTITY)).thenReturn(false);
    cardService.deleteCard(CARD_DTO.getId());
  }

  @Test(expected = CardNotFoundException.class)
  public void whenRepoCannotFindTheCardWeWantToDelete_thenCardNotFoundExceptionIsThrown() {
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.empty());
    cardService.deleteCard(CARD_DTO.getId());
  }

  /* PUT tests */

  @Test
  public void whenRepoFindsTheEntityToReplace_andUserHasAccess_thenReplacementIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    CardDto replacementCard =
        CardDto.builder()
            .id(CARD_DTO.getId())
            .name(CARD_DTO.getName() + " - REPLACEMENT")
            .status(CardStatus.IN_PROGRESS)
            .build();
    when(cardRepository.findById(replacementCard.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(ADMIN_USER, CARD_ENTITY)).thenReturn(true);
    when(cardRepository.save(any(CardEntity.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    assertTrue(
        cardDtosEqual(
            replacementCard, cardService.replaceCard(replacementCard.getId(), replacementCard)));
  }

  @Test(expected = InsufficientPrivilegesException.class)
  public void
      whenRepoFindsTheEntityToReplace_butUserDoesNotHaveAccess_thenInsufficientPrivilegesExceptionIsThrown() {
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    CardDto replacementCard =
        CardDto.builder()
            .id(CARD_DTO.getId())
            .name(CARD_DTO.getName() + " - REPLACEMENT")
            .status(CardStatus.IN_PROGRESS)
            .build();
    when(cardRepository.findById(replacementCard.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(MEMBER_USER, CARD_ENTITY)).thenReturn(false);
    cardService.replaceCard(replacementCard.getId(), replacementCard);
  }

  @Test(expected = CardNotFoundException.class)
  public void whenRepoCannotFindTheEntityToReplace_thenCardNotFoundExceptionIsThrown() {
    CardDto replacementCard =
        CardDto.builder()
            .id(CARD_DTO.getId())
            .name(CARD_DTO.getName() + " - REPLACEMENT")
            .status(CardStatus.IN_PROGRESS)
            .build();
    when(cardRepository.findById(replacementCard.getId())).thenReturn(Optional.empty());
    cardService.replaceCard(replacementCard.getId(), replacementCard);
  }

  /* PATCH tests */
  
  @Test
  public void whenRepoFindsTheEntityToUpdate_andUserHasAccess_thenOnlySpecifiedFieldsAreUpdated(){
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    CardDto patch = CardDto.builder()
            .id(CARD_DTO.getId())
            .name("PATCH")
            .description("A patch to card with ID: " + CARD_DTO.getId())
            .build();
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(ADMIN_USER, CARD_ENTITY)).thenReturn(true);
    doAnswer(invocationOnMock -> {
      CardDto cardDto = invocationOnMock.getArgument(0);
      CardEntity cardEntity = invocationOnMock.getArgument(1);
      cardEntity.setName(cardDto.getName());
      cardEntity.setDescription(cardDto.getDescription());
      return null;
    }).when(patchMapper).updateEntityFromDto(patch, CARD_ENTITY);
    when(cardRepository.save(any(CardEntity.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    assertTrue(cardDtosEqual(
            CardDto.builder()
                    .id(CARD_DTO.getId())
                    .name(patch.getName())
                    .description(patch.getDescription())
                    .status(CARD_ENTITY.getStatus())
                    .color(CARD_ENTITY.getColor())
                    .build(), cardService.updateCard(patch.getId(), patch)));
  }
  
  @Test(expected = InsufficientPrivilegesException.class)
  public void whenRepoFindsTheEntityToUpdate_butUserDoesNotHaveAccess_thenInsufficientPrivilegesExceptionIsThrown(){
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    CardDto patch = CardDto.builder()
            .id(CARD_DTO.getId())
            .name("PATCH")
            .description("A patch to card with ID: " + CARD_DTO.getId())
            .build();
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(MEMBER_USER, CARD_ENTITY)).thenReturn(false);
    cardService.updateCard(CARD_DTO.getId(), patch);
  }
  
  @Test(expected = CardNotFoundException.class)
  public void whenRepoCannotFindTheEntityToUpdate_thenCardNotFoundExceptionIsThrown(){
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    CardDto patch = CardDto.builder()
            .id(CARD_DTO.getId())
            .name("PATCH")
            .description("A patch to card with ID: " + CARD_DTO.getId())
            .build();
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.empty());
    cardService.updateCard(CARD_DTO.getId(), patch);
  }
}