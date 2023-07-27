package com.logicea.cardsapp.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class UserDto {

    @Schema(example = "jason.filippou@gmail.com")
    @NonNull
    @NotBlank
    @Email
    private String email;

    @Schema(example = "jasonfilpassword")
    @JsonProperty(access = WRITE_ONLY)
    @NonNull
    @ToString.Exclude
    @Size(min = 8, max = 30)
    private String password;

    @Schema(example = "ADMIN")
    private UserRole role;
}