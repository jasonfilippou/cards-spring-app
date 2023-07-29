package com.logicea.cardsapp.model;

import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public @NonNull Optional<String> getCurrentAuditor() {
    return Optional.of(
        ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getUsername());
  }
}
