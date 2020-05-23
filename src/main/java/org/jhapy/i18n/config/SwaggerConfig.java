package org.jhapy.i18n.config;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Value("${security.oauth2.client.accessTokenUri}")
  private String accessTokenUri;
  @Value("${security.oauth2.client.userAuthorizationUri}")
  private String userAuthorizationUri;
  @Value("${security.oauth2.client.clientId}")
  private String clientId;
  @Value("${security.oauth2.client.clientSecret}")
  private String clientSecret;

  @Bean
  public Docket api() {
    // @formatter:off
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("org.jhapy.i18n.endpoint"))
        .paths(PathSelectors.any())
        .build()
        .securitySchemes( Arrays.asList( securityScheme() ) )
        .securityContexts( Arrays.asList( securityContext() ) );
    // @formatter:on
  }

  private ApiInfo apiInfo() {
    // @formatter:off
    return new ApiInfo(
        "jHapy I18N Server REST API",
        "Rest API documentation for jHapy I18N Server project.",
        "API v1.0",
        "Terms of service",
        new Contact("jHapy", "", "jhapy@jhapy.org"),
        "License of API",
        "API license URL",
        Collections.emptyList());
    // @formatter:on
  }

  @Bean
  public SecurityConfiguration security() {
    return SecurityConfigurationBuilder.builder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .useBasicAuthenticationWithAccessCodeGrant(true)
        .build();
  }

  private SecurityScheme securityScheme() {
    // @formatter:off
    GrantType grantType =
        new AuthorizationCodeGrantBuilder()
            .tokenEndpoint(new TokenEndpoint(accessTokenUri, "oauthtoken"))
            .tokenRequestEndpoint(
                new TokenRequestEndpoint(userAuthorizationUri, clientId, clientSecret))
            .build();
    // @formatter:on
    return new OAuthBuilder()
        .name("spring_oauth")
        .grantTypes(Collections.singletonList(grantType))
        .scopes(Arrays.asList(scopes()))
        .build();
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(
            Collections.singletonList(new SecurityReference("spring_oauth", scopes())))
        .forPaths(PathSelectors.regex("/action.*"))
        .forPaths(PathSelectors.regex("/element.*"))
        .forPaths(PathSelectors.regex("/message.*"))
        .build();
  }

  private AuthorizationScope[] scopes() {
    return new AuthorizationScope[]{
        new AuthorizationScope("frontend", "for frontend application"),
        new AuthorizationScope("mobile", "for mobile application"),
        new AuthorizationScope("backend", "for backend server")
    };
  }
}
