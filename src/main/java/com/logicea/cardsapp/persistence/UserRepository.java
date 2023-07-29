package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.user.UserEntity;
import com.logicea.cardsapp.util.logger.Logged;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@Logged
public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);
}
