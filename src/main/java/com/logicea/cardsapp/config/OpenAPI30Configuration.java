package com.logicea.cardsapp.config;

import static com.logicea.cardsapp.util.Constants.*;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration class that configures the OpenAPI 3 components for our app.
 *
 * @author jason
 */
@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Agile Bank API",
            version = "${api.version}",
            contact =
                @Contact(
                    name = "Jason Filippou",
                    email = "jason.filippou@gmail.com",
                    url = "https://www.jasonfilippou.com"),
            license =
                @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
            termsOfService = "${tos.uri}",
            description = "${api.description}"),
    servers = {
      @Server(url = "http://localhost:8080", description = "Development"),
      @Server(url = "${api.server.url}", description = "Production")
    })
public class OpenAPI30Configuration {

  /**
   * Configure the OpenAPI components.
   *
   * @return Returns fully configured OpenAPI object
   * @see OpenAPI
   */
  @Bean
  public OpenAPI customizeOpenAPI() {
    final String securitySchemeName = "bearerAuth";
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSchemas(
                    "FilterMap",
                    new Schema<Map<String, String>>()
                        .addProperty(NAME_FILTER_STRING, new StringSchema().example("Standup"))
                        .addProperty(COLOR_FILTER_STRING, new StringSchema().example("#5P091H"))
                        .addProperty(
                            STATUS_FILTER_STRING, new StringSchema().example("IN_PROGRESS"))
                        .addProperty(
                            BEGIN_CREATION_DATE_FILTER_STRING,
                            new StringSchema().example("12/07/2012 00:00:00.000"))
                        .addProperty(
                            END_CREATION_DATE_FILTER_STRING,
                            new StringSchema().example("12/07/2012 01:00:00.000")))
                .addSchemas(
                    "FullCardDto",
                    new Schema<Map<String, String>>()
                        .addProperty(NAME_FILTER_STRING, new StringSchema().example("Standup"))
                        .addProperty("description", new StringSchema().example("Daily standup meeting"))
                        .addProperty(COLOR_FILTER_STRING, new StringSchema().example("#5P091H"))
                        .addProperty(STATUS_FILTER_STRING, new StringSchema().example("TODO"))
                        .addProperty(
                            "createdBy", new StringSchema().example("d.polissiou@logicea.com"))
                        .addProperty(
                            "createdDateTime",
                            new StringSchema().example("12/07/2012 00:00:00.000"))
                        .addProperty(
                            "lastModifiedBy", new StringSchema().example("d.polissiou@logicea.com"))
                        .addProperty(
                            "lastModifiedDateTime",
                            new StringSchema().example("12/07/2012 01:00:00.000")))
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description(
                            "Provide the JWT token. JWT token can be obtained from the \"authenticate\" endpoint.")
                        .bearerFormat("JWT")));
  }
}
