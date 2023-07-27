package com.logicea.cardsapp.model.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.*;

/**
 * Simple POJO for defining a user request for a JWT token.
 * 
 * @author jason 
 * 
 * @see JwtResponse
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class JwtRequest implements Serializable {

  private static final long serialVersionId = 5926468583005150707L;

  @Schema(example = "jason.filippou@gmail.com")
  @NonNull @NotBlank
  @Email private String email;
  @Schema(example = "jasonfilpassword")
  @Size(min = 8, max = 30)
  @NonNull @ToString.Exclude String password;
}
