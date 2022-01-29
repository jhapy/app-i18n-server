/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.i18n.config;

import org.jhapy.commons.config.AppProperties;
import org.jhapy.commons.security.oauth2.AudienceValidator;
import org.jhapy.commons.security.oauth2.JwtGrantedAuthorityConverter;
import org.jhapy.commons.utils.HasLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter implements HasLogger {

  private final AppProperties appProperties;
  private final SecurityProblemSupport problemSupport;

  @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
  private String issuerUri;

  public SecurityConfiguration(AppProperties appProperties, SecurityProblemSupport problemSupport) {
    this.problemSupport = problemSupport;
    this.appProperties = appProperties;
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http.cors()
        .and()
        .csrf()
        .disable()
        .cors()
        .configurationSource(corsConfigurationSource())
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(problemSupport)
        .accessDeniedHandler(problemSupport)
        .and()
        .headers()
        .contentSecurityPolicy(
            "default-src 'self' "
                + appProperties.getKeycloakAdmin().getServerUrl()
                + "; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
        .and()
        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
        .featurePolicy(
            "geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
        .and()
        .frameOptions()
        .disable()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/api/auth-info")
        .permitAll()
        .antMatchers("/api/i18NService/getExistingLanguages")
        .permitAll()
        .antMatchers("/api/elementService/save*")
        .hasAnyAuthority("ROLE_I18N_ADMIN", "ROLE_I18N_WRITE")
        .antMatchers("/api/elementService/delete*")
        .hasAnyAuthority("ROLE_I18N_ADMIN", "ROLE_I18N_WRITE")
        .antMatchers("/api/elementService/**")
        .permitAll()
        .antMatchers("/api/actionService/save*")
        .hasAnyAuthority("ROLE_I18N_ADMIN", "ROLE_I18N_WRITE")
        .antMatchers("/api/actionService/delete*")
        .hasAnyAuthority("ROLE_I18N_ADMIN", "ROLE_I18N_WRITE")
        .antMatchers("/api/actionService/**")
        .permitAll()
        .antMatchers("/api/messageService/save*")
        .hasAnyAuthority("ROLE_I18N_ADMIN", "ROLE_I18N_WRITE")
        .antMatchers("/api/messageService/delete*")
        .hasAnyAuthority("ROLE_I18N_ADMIN", "ROLE_I18N_WRITE")
        .antMatchers("/api/messageService/**")
        .permitAll()
        .antMatchers("/api/**")
        .authenticated()
        .antMatchers("/management/health")
        .permitAll()
        .antMatchers("/management/health/**")
        .permitAll()
        .antMatchers("/management/info")
        .permitAll()
        .antMatchers("/management/prometheus")
        .permitAll()
        .antMatchers("/management/**")
        .hasAuthority("ROLE_ADMIN")
        .and()
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(authenticationConverter())
        .and()
        .and()
        .oauth2Client();
    // @formatter:on
  }

  Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
    var jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
        new JwtGrantedAuthorityConverter());
    return jwtAuthenticationConverter;
  }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator =
        new AudienceValidator(appProperties.getSecurity().getOauth2().getAudience());
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience =
        new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    var loggerPrefix = getLoggerPrefix("corsConfigurationSource");
    var source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = appProperties.getCors();
    config.applyPermitDefaultValues();

    if (config.getAllowedOrigins() != null && !config.getAllowedOrigins().isEmpty()) {
      debug(loggerPrefix, "Registering CORS filter");
      source.registerCorsConfiguration("/config/**", config);
      source.registerCorsConfiguration("/eureka/**", config);
      source.registerCorsConfiguration("/api/**", config);
      source.registerCorsConfiguration("/management/**", config);
      source.registerCorsConfiguration("/v2/api-docs", config);
      source.registerCorsConfiguration("/swagger-ui.html**", config);
    }

    return source;
  }
}
