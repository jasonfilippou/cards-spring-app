package com.logicea.cardsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Central Spring Boot application class. Runs the entire container.
 *
 * @author jason
 */
@SpringBootApplication
public class CardsApplication {

  public static void main(String[] args) {
    SpringApplication.run(CardsApplication.class, args);
  }
}
