package com.logicea.cardsapp.util;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class AggregateGetQueryParams {
    private Integer page;
    private Integer pageSize;
    private String sortByField;
    private SortOrder sortOrder;
    private Map<String, String> filterParams;
}