package com.example.blog.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("dev")
public class FlywayDevRepairConfig {

  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy(Environment environment) {
    boolean repairOnStartup = Boolean.parseBoolean(environment.getProperty("app.flyway.repair-on-startup", "false"));
    return (Flyway flyway) -> {
      if (repairOnStartup) {
        flyway.repair();
      }
      flyway.migrate();
    };
  }
}
