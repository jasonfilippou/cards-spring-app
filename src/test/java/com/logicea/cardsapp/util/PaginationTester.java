package com.logicea.cardsapp.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class PaginationTester {
  @NonNull private final Integer totalPages;
  @NonNull private final Integer pageSize;
  private final Map<String, String> filterParams;
  @NonNull private final Class<?> pojoType;

  public void runTest(BiConsumer<AggregateGetQueryParams, Integer> test) {
    for (int i = 0; i < totalPages; i++) {
      final int finalI =  i; // Vars used in lambda expressions (below) should be final of effectively final.
      Arrays.stream(pojoType.getFields())
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
                                      .filterParams(filterParams)
                                      .build(),
                                  expectedPageNumber(finalI, totalPages, pageSize))));
    }
  }
  
  private int expectedPageNumber(int currentPageIdx, int totalPageNum, int pageSize){
      if((currentPageIdx < totalPageNum - 1) || (totalPageNum % pageSize == 0)){
          return pageSize;
      }
      return totalPageNum % pageSize;
  }
}
