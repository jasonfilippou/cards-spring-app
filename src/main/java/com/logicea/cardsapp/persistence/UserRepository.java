package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.user.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@LoggedRepository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);
}
