package uk.ac.rhul.cs3821;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
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