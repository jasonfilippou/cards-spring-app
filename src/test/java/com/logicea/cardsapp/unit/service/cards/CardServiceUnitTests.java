package com.logicea.cardsapp.unit.service.cards;

import static com.logicea.cardsapp.util.Constants.*;
import static com.logicea.cardsapp.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.logicea.cardsapp.model.card.CardDto;
import com.logicea.cardsapp.model.card.CardEntity;
import com.logicea.cardsapp.model.card.CardStatus;
import com.logicea.cardsapp.persistence.CardRepository;
import com.logicea.cardsapp.service.cards.AccessCheckService;
import com.logicea.cardsapp.service.cards.CardService;
import com.logicea.cardsapp.util.AggregateGetQueryParams;
import com.logicea.cardsapp.util.PaginationTester;
import com.logicea.cardsapp.util.SortOrder;
import com.logicea.cardsapp.util.exceptions.CardNotFoundException;
import com.logicea.cardsapp.util.exceptions.InsufficientPrivilegesException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    return cardDto.getId().equals(cardEntity.getId())
        && cardDto.getStatus().equals(cardEntity.getStatus())
        && cardDto.getDescription().equals(cardEntity.getDescription())
        && cardDto.getColor().equals(cardEntity.getColor())
        && cardDto.getName().equals(cardEntity.getName());
  }
  
  // We will do the same for two CardDto instances.
  private static boolean cardDtosEqual(CardDto dtoOne, CardDto dtoTwo){
    return dtoOne.getId().equals(dtoTwo.getId())
            && dtoOne.getStatus().equals(dtoTwo.getStatus())
            && dtoOne.getDescription().equals(dtoTwo.getDescription())
            && dtoOne.getColor().equals(dtoTwo.getColor())
            && dtoOne.getName().equals(dtoTwo.getName());
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

    PaginationTester.builder()
        .totalPages(1)
        .pageSize(20)
        .pojoType(CardDto.class)
        .filterParams(Collections.emptyMap())
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithoutFilters);

    // Second, test 2 pages of 10 cards each.
    PaginationTester.builder()
        .totalPages(2)
        .pageSize(10)
        .pojoType(CardDto.class)
        .filterParams(Collections.emptyMap())
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithoutFilters);

    // Third, test 4 pages of 5 cards each.
    PaginationTester.builder()
        .totalPages(4)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filterParams(Collections.emptyMap())
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithoutFilters);

    // Fourth and final, test 7 pages with 3, 3, 3, 3, 3, 3 and 2 cards respectively.

    PaginationTester.builder()
        .totalPages(7)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filterParams(Collections.emptyMap())
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithoutFilters);
  }

  private void testPaginatedAndSortedAggregateGetWithoutFilters(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    List<CardEntity> expectedList =
        CARD_ENTITIES.stream()
            .sorted(
                (t1, t2) ->
                    compareFieldsInGivenOrder(t1.getClass(), t2.getClass(), sortByField, sortOrder))
            .toList()
            .subList(page * pageSize, pageSize * (page + 1));
    mockWithExpectedList(params, expectedList);
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
    assertEquals(returnedDtos.size(), expectedNumEntries);
    assertTrue(collectionIsSortedByFieldInGivenDirection(returnedDtos, sortByField, sortOrder));
  }

  @Test
  public void whenRepoReturnsASortedPage_andNameFilterIsUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 9 cards that share the same name.

    // First, test a page with 9 cards.

    PaginationTester.builder()
        .totalPages(1)
        .pageSize(9)
        .pojoType(CardDto.class)
        .filterParams(Map.of(NAME_FILTER_STRING, NAME))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithNameFilter);

    // Second, test 3 pages with 3 cards each.

    PaginationTester.builder()
        .totalPages(3)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filterParams(Map.of(NAME_FILTER_STRING, NAME))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithNameFilter);

    // Third and final, test 3 pages, with 4 + 4 + 1 cards each, respectively.

    PaginationTester.builder()
        .totalPages(3)
        .pageSize(4)
        .pojoType(CardDto.class)
        .filterParams(Map.of(NAME_FILTER_STRING, NAME))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithNameFilter);
  }

  private void testPaginatedAndSortedAggregateGetWithNameFilter(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    Map<String, String> filters = params.getFilterParams();
    assert filters.containsKey(NAME_FILTER_STRING);
    List<CardEntity> expectedList =
        CARD_ENTITIES.stream()
            .filter(cardEntity -> cardEntity.getName().equals(filters.get(NAME_FILTER_STRING)))
            .sorted(
                (t1, t2) ->
                    compareFieldsInGivenOrder(t1.getClass(), t2.getClass(), sortByField, sortOrder))
            .toList()
            .subList(page * pageSize, pageSize * (page + 1));
    mockWithExpectedList(params, expectedList);
    makePaginationAndSortingQueryAssertions(
        cardService.getAllCardsByFilter(params), expectedNumEntries, sortByField, sortOrder);
  }

  @Test
  public void whenRepoReturnsASortedPage_andColorFilterIsUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 5 cards that share the same color.

    // First, test a page with 5 cards.

    PaginationTester.builder()
        .totalPages(1)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filterParams(Map.of(COLOR_FILTER_STRING, COLOR))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithColorFilter);

    // Second, test 2 pages, one with 3 and another one with 2 cards.

    PaginationTester.builder()
        .totalPages(2)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filterParams(Map.of(COLOR_FILTER_STRING, COLOR))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithColorFilter);

    // Third and final, test 3 pages, with 2 + 2 + 1 cards each, respectively.

    PaginationTester.builder()
        .totalPages(3)
        .pageSize(2)
        .pojoType(CardDto.class)
        .filterParams(Map.of(COLOR_FILTER_STRING, COLOR))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithColorFilter);
  }

  private void testPaginatedAndSortedAggregateGetWithColorFilter(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    Map<String, String> filters = params.getFilterParams();
    assert filters.containsKey(COLOR_FILTER_STRING);
    List<CardEntity> expectedList =
        CARD_ENTITIES.stream()
            .filter(cardEntity -> cardEntity.getColor().equals(filters.get(COLOR_FILTER_STRING)))
            .sorted(
                (t1, t2) ->
                    compareFieldsInGivenOrder(t1.getClass(), t2.getClass(), sortByField, sortOrder))
            .toList()
            .subList(page * pageSize, pageSize * (page + 1));
    mockWithExpectedList(params, expectedList);
    makePaginationAndSortingQueryAssertions(
        cardService.getAllCardsByFilter(params), expectedNumEntries, sortByField, sortOrder);
  }

  @Test
  public void whenRepoReturnsASortedPage_andStatusFilterIsUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 8 cards that share the same status.

    // First, test a page with 8 cards.

    PaginationTester.builder()
        .totalPages(1)
        .pageSize(8)
        .pojoType(CardDto.class)
        .filterParams(Map.of(STATUS_FILTER_STRING, STATUS.toString()))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithStatusFilter);

    // Second, test 2 pages, one with 5 and another one with 3 cards.

    PaginationTester.builder()
        .totalPages(2)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filterParams(Map.of(STATUS_FILTER_STRING, STATUS.toString()))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithStatusFilter);
  }

  private void testPaginatedAndSortedAggregateGetWithStatusFilter(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    Map<String, String> filters = params.getFilterParams();
    assert filters.containsKey(STATUS_FILTER_STRING);
    List<CardEntity> expectedList =
        CARD_ENTITIES.stream()
            .filter(
                cardEntity ->
                    cardEntity.getStatus().toString().equals(filters.get(STATUS_FILTER_STRING)))
            .sorted(
                (t1, t2) ->
                    compareFieldsInGivenOrder(t1.getClass(), t2.getClass(), sortByField, sortOrder))
            .toList()
            .subList(page * pageSize, pageSize * (page + 1));
    mockWithExpectedList(params, expectedList);
    makePaginationAndSortingQueryAssertions(
        cardService.getAllCardsByFilter(params), expectedNumEntries, sortByField, sortOrder);
  }

  @Test
  public void whenRepoReturnsASortedPage_andDateOfCreationFiltersAreUsed_thenThatPageIsReturned() {
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);

    // There are 5 cards created in the set {now() - 5min, now() - 4min, ... now() - 1min}.

    // First, test a page with 5 cards.

    PaginationTester.builder()
        .totalPages(1)
        .pageSize(5)
        .pojoType(CardDto.class)
        .filterParams(
            Map.of(
                BEGIN_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().minusMinutes(5).format(DATE_TIME_FORMATTER),
                END_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().format(DATE_TIME_FORMATTER)))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithCreationDateFilters);

    // Second, test 2 pages, one with 4 and another one with 1 card.

    PaginationTester.builder()
        .totalPages(2)
        .pageSize(4)
        .pojoType(CardDto.class)
        .filterParams(
            Map.of(
                BEGIN_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().minusMinutes(5).format(DATE_TIME_FORMATTER),
                END_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().format(DATE_TIME_FORMATTER)))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithCreationDateFilters);
  }

  private void testPaginatedAndSortedAggregateGetWithCreationDateFilters(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    Map<String, String> filters = params.getFilterParams();
    assert filters.containsKey(BEGIN_CREATION_DATE_FILTER_STRING)
        && filters.containsKey(END_CREATION_DATE_FILTER_STRING);
    List<CardEntity> expectedList =
        CARD_ENTITIES.stream()
            .filter(
                cardEntity ->
                    creationDateBetween(
                        cardEntity.getCreatedDateTime(),
                        LocalDateTime.parse(
                            filters.get(BEGIN_CREATION_DATE_FILTER_STRING), DATE_TIME_FORMATTER),
                        LocalDateTime.parse(
                            filters.get(END_CREATION_DATE_FILTER_STRING), DATE_TIME_FORMATTER)))
            .sorted(
                (t1, t2) ->
                    compareFieldsInGivenOrder(t1.getClass(), t2.getClass(), sortByField, sortOrder))
            .toList()
            .subList(page * pageSize, pageSize * (page + 1));
    mockWithExpectedList(params, expectedList);
    makePaginationAndSortingQueryAssertions(
        cardService.getAllCardsByFilter(params), expectedNumEntries, sortByField, sortOrder);
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
    PaginationTester.builder()
        .totalPages(1)
        .pageSize(3)
        .pojoType(CardDto.class)
        .filterParams(
            Map.of(
                BEGIN_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().minusMinutes(5).format(DATE_TIME_FORMATTER),
                END_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().format(DATE_TIME_FORMATTER),
                STATUS_FILTER_STRING,
                STATUS.toString()))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithStatusAndCreationDateFilters);

    // Second, test 2 pages, 1 with 2 and 1 with 1 card.
    PaginationTester.builder()
        .totalPages(2)
        .pageSize(2)
        .pojoType(CardDto.class)
        .filterParams(
            Map.of(
                BEGIN_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().minusMinutes(5).format(DATE_TIME_FORMATTER),
                END_CREATION_DATE_FILTER_STRING,
                LocalDateTime.now().format(DATE_TIME_FORMATTER),
                STATUS_FILTER_STRING,
                STATUS.toString()))
        .build()
        .runTest(this::testPaginatedAndSortedAggregateGetWithStatusAndCreationDateFilters);
  }

  private void testPaginatedAndSortedAggregateGetWithStatusAndCreationDateFilters(
      AggregateGetQueryParams params, int expectedNumEntries) {
    Integer page = params.getPage();
    Integer pageSize = params.getPageSize();
    String sortByField = params.getSortByField();
    SortOrder sortOrder = params.getSortOrder();
    Map<String, String> filters = params.getFilterParams();
    assert filters.containsKey(BEGIN_CREATION_DATE_FILTER_STRING)
        && filters.containsKey(END_CREATION_DATE_FILTER_STRING)
        && filters.containsKey(STATUS_FILTER_STRING);
    List<CardEntity> expectedList =
        CARD_ENTITIES.stream()
            .filter(
                cardEntity ->
                    creationDateBetween(
                            cardEntity.getCreatedDateTime(),
                            LocalDateTime.parse(
                                filters.get(BEGIN_CREATION_DATE_FILTER_STRING),
                                DATE_TIME_FORMATTER),
                            LocalDateTime.parse(
                                filters.get(END_CREATION_DATE_FILTER_STRING), DATE_TIME_FORMATTER))
                        && cardEntity
                            .getStatus()
                            .toString()
                            .equals(filters.get(STATUS_FILTER_STRING)))
            .sorted(
                (t1, t2) ->
                    compareFieldsInGivenOrder(t1.getClass(), t2.getClass(), sortByField, sortOrder))
            .toList()
            .subList(page * pageSize, pageSize * (page + 1));
    mockWithExpectedList(params, expectedList);
    makePaginationAndSortingQueryAssertions(
        cardService.getAllCardsByFilter(params), expectedNumEntries, sortByField, sortOrder);
  }
  
  @Test(expected = InsufficientPrivilegesException.class)
  public void whenAttemptingToGetADifferentUsersCards_whileNotAdmin_thenInsufficientPrivilegesExceptionIsThrown(){
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    AggregateGetQueryParams paramsWithOtherUsersEmail = 
            AggregateGetQueryParams.builder()
                    .filterParams(Map.of(CREATING_USER_FILTER_STRING, ADMIN_EMAIL)).build();
    // Mocking just for consistency, since this would return true anyhow.
    when(accessCheckService.userIsMember(MEMBER_USER)).thenReturn(true);
    cardService.getAllCardsByFilter(paramsWithOtherUsersEmail);
  }

  /* POST tests */
  
  @Test
  public void whenRepoPersistsACardSuccessfully_thenTheCardIsReturned(){
    when(cardRepository.save(any(CardEntity.class))).thenReturn(CARD_ENTITY);
    assertTrue(cardDtosEqual(CARD_DTO, cardService.storeCard(CARD_DTO)));
  }

  /* DELETE tests */

  @Test
  public void whenRepoFindsTheCardById_andUserHasAccessToIt_thenDeleteShouldWork(){
    SecurityContextHolder.getContext().setAuthentication(ADMIN_UPAT);
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(ADMIN_USER, CARD_ENTITY)).thenReturn(true); 
    doNothing().when(cardRepository).deleteById(CARD_DTO.getId());
    cardService.deleteCard(CARD_DTO.getId());
  }

  @Test(expected = InsufficientPrivilegesException.class)
  public void whenRepoFindsTheCardById_andUserDoesNotHaveAccessToIt_thenInsufficientPrivilegesExceptionIsThrown(){
    SecurityContextHolder.getContext().setAuthentication(MEMBER_UPAT);
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.of(CARD_ENTITY));
    when(accessCheckService.userHasAccessToCard(MEMBER_USER, CARD_ENTITY)).thenReturn(false);
    cardService.deleteCard(CARD_DTO.getId());
  }
  
  @Test(expected = CardNotFoundException.class)
  public void whenRepoCannotFindTheCardWeWantToDelete_thenCardNotFoundExceptionIsThrown(){
    when(cardRepository.findById(CARD_DTO.getId())).thenReturn(Optional.empty());
    cardService.deleteCard(CARD_DTO.getId());
  }
  
  /* PUT tests */


  /* PATCH tests */

  // TODO
}
