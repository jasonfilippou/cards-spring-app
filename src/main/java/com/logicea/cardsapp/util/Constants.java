package com.logicea.cardsapp.util;

public final class Constants {
    private Constants(){}

    public static final String AUTH_HEADER_BEARER_PREFIX = "Bearer" + " ";

    /**
     * Tune this to affect how long the JWT token lasts. Default is 5 * 60 * 60, for 5 hours.
     */
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours
}
