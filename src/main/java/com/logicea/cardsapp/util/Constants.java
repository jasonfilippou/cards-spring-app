package com.logicea.cardsapp.util;

import com.logicea.cardsapp.model.user.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    // The following Strings are going to play the role of keys for our filter map.

    public static final String NAME_FILTER_STRING = "name";
    public static final String COLOR_FILTER_STRING = "color";
    public static final String STATUS_FILTER_STRING = "status";
    public static final String BEGIN_CREATION_DATE_FILTER_STRING = "begin_date_created";
    public static final String END_CREATION_DATE_FILTER_STRING = "end_date_created";
    public static final String CREATING_USER_FILTER_STRING = "created_by";

  // Two instances of SimpleGrantedAuthority corresponding to our user roles of Member or Admin.

  public static final SimpleGrantedAuthority ADMIN_AUTHORITY =
      new SimpleGrantedAuthority(UserRole.ADMIN.name());

    public static final SimpleGrantedAuthority MEMBER_AUTHORITY =
            new SimpleGrantedAuthority(UserRole.MEMBER.name());

    // Our global date-time pattern, with accuracy up to seconds.

    public static final String GLOBAL_DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm:ss.SSS";
}
