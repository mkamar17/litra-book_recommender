package uk.ac.rhul.cs3821;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class FypApplication {

  /**
   * Application entry point.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(FypApplication.class, args);
  }
}