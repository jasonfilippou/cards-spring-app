package com.logicea.cardsapp.model.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO class for Cards.
 *
 * @author jason
 *
 * @see CardEntity
 */
@Data
@Builder
@EqualsAndHashCode
public class CardDto {
    @NonNull @NotBlank
    private String name;

    @Size(max = 100)
    private String description;

    @Pattern(regexp = "^#[a-zA-Z0-9]{6}$", message = "Color must start with # and end with exactly 6 alphanumerics",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    @Size(min = 7, max = 7)
    private String color;

    @Builder.Default
    private CardStatus status = CardStatus.TODO;
}
