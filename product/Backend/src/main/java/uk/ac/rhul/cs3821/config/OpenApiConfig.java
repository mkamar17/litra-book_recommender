package uk.ac.rhul.cs3821.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config class to permit SwaggerUI to use JWT Bearer Auth.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Defines OpenAI config used by Swagger.
   *
   * @return an OpenAI instance configured with JWT Bearer Authentication.s
   */
  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new Components().addSecuritySchemes(
                "bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        );
  }
}
