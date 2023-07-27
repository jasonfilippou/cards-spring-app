package com.logicea.cardsapp.persistence;

import com.logicea.cardsapp.model.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@LoggedRepository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}