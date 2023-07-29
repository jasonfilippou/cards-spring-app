package com.logicea.cardsapp.model.card;

import static com.logicea.cardsapp.util.Constants.GLOBAL_DATE_TIME_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO class for Cards.
 *
 * @author jason
 * @see CardEntity
 */
@Data
@Builder
@EqualsAndHashCode
public class CardDto {
  @Schema(example = "1", hidden = true)
  private Long id;

  @NonNull
  @NotBlank
  @Size(max = 50)
  private String name;

  @Size(max = 100)
  private String description;

  @Pattern(
      regexp = "^#[a-zA-Z0-9]{6}$",
      message = "Color must start with # and end with exactly 6 alphanumerics",
      flags = Pattern.Flag.CASE_INSENSITIVE)
  private String color;

  @Builder.Default private CardStatus status = CardStatus.TODO;

  @DateTimeFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @JsonFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private LocalDateTime createdDateTime;

  @Size(min = 5, max = 50)
  @Email
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String createdBy;

  @DateTimeFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @JsonFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private LocalDateTime lastModifiedDateTime;

  @Size(min = 5, max = 50)
  @Email
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String lastModifiedBy;
}
