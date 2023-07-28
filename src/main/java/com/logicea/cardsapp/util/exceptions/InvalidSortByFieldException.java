package com.logicea.cardsapp.util.exceptions;

import lombok.Getter;

import java.util.List;

/**
 * A {@link RuntimeException} thrown in paginated queries when the user provides an invalid field to sort by.
 *
 * @author jason
 */
@Getter
public class InvalidSortByFieldException extends RuntimeException{

    private final String fieldSpecified;
    private final List<String> acceptableFields;
    public InvalidSortByFieldException(String fieldSpecified, List<String> acceptableFields){
        super("Invalid sort by field: " + fieldSpecified + " specified. Acceptable fields are: " +
                acceptableFields.toString() + ".");
        this.fieldSpecified = fieldSpecified;
        this.acceptableFields = List.copyOf(acceptableFields);
    }
}