package com.logicea.cardsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Defines a Spring Bean of type {@link PasswordEncoder} for the various components to use.
 *
 * @author jason
 * @see SecurityConfig
 */
@Configuration
public class PasswordEncoderConfig {

  /**
   * Define a Spring Bean of type {@link PasswordEncoder} for the various components to use.
   *
   * @return An instance of type {@link PasswordEncoder}.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
