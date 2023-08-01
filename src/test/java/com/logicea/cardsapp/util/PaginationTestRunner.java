package com.logicea.cardsapp.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A class that facilitates testing of pagination / sorting / filtering queries.
 * @param <T> The DTO type of the elements of the collection that will be compared against each other for sorting purposes.
 * @param <U> The Entity type of the elements of the collection used for hard-coding the returned values of the mocked repository.
 *           
 * @author jason 
 */
@Builder
@Getter
@RequiredArgsConstructor
public class PaginationTestRunner<T, U> {
  @NonNull private final Integer totalPages;
  @NonNull private final Integer pageSize;
  @NonNull private final Class<T> pojoType;
  private final Predicate<U> filteringPredicate;
  @NonNull private final List<Integer> expectedPageSizes;


  /**
   * Run an exhaustive test over all parameters of {@literal this}.
   * @param test A {@link BiConsumer} that will perorm the actual test.
   */
  public void runTest(BiConsumer<AggregateGetQueryParams, Integer> test) {
    for (int i = 0; i < totalPages; i++) {
      final int finalI =  i; // Vars used in lambda expressions (below) should be final of effectively final.
      Arrays.stream(pojoType.getDeclaredFields())
          .map(Field::getName)
          .toList()
          .forEach(
              fieldName ->
                  List.of(SortOrder.ASC, SortOrder.DESC)
                      .forEach(
                          sortOrder ->
                              test.accept(
                                  AggregateGetQueryParams.builder()
                                      .page(finalI)
                                      .pageSize(pageSize)
                                      .sortByField(fieldName)
                                      .sortOrder(sortOrder)
                                      .predicate(filteringPredicate)
                                      .build(),
                                  expectedPageSizes.get(finalI))));
    }
  }
}
