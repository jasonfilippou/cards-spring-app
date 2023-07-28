package com.logicea.cardsapp.util;

/**
 * Global named constants useful for our application.
 *
 * @author jason
 */
public final class Constants {
    private Constants(){}

    public static final String AUTH_HEADER_BEARER_PREFIX = "Bearer" + " ";

    public static final String ALL_CARDS = "all_cards";


    /**
     * Tune this to affect how long the JWT token lasts. Default is 5 * 60 * 60, for 5 hours.
     */
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours

    // Some constants for pagination and sorting
    public static final String DEFAULT_PAGE_IDX = "0";
    public static final String DEFAULT_PAGE_SIZE = "5";
    public static final String DEFAULT_SORT_BY_FIELD = "id";
    public static final String DEFAULT_SORT_ORDER = "ASC";
}
