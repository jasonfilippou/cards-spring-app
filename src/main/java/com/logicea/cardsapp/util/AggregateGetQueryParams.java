package com.logicea.cardsapp.util;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AggregateGetQueryParams {
  private Integer page;
  private Integer pageSize;
  private String sortByField;
  private SortOrder sortOrder;
  private Map<String, String> filterParams;
}
