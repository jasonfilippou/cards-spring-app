package com.logicea.cardsapp.model.user;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO class for Users.
 * 
 * @see UserEntity
 * 
 * @author jason 
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class UserDto {

  @Schema(example = "m.giannarou@logicea.com")
  @NonNull
  @NotBlank
  @Email
  @Size(min = 5, max = 50)
  private String email;

  @Schema(example = "mgiannaroupass")
  @JsonProperty(access = WRITE_ONLY)
  @NonNull
  @ToString.Exclude
  @Size(min = 8, max = 30)
  private String password;

  @Schema(example = "ADMIN")
  @NonNull
  private UserRole role;
}
