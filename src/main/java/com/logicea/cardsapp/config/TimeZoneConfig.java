package com.logicea.cardsapp.config;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeZoneConfig {

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.systemDefault()));
  }
}
