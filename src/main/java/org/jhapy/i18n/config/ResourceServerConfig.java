package org.jhapy.i18n.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  private final ResourceServerProperties sso;

  private final OAuth2ClientContext oAuth2ClientContext;

  @Autowired
  public ResourceServerConfig(ResourceServerProperties sso,
      OAuth2ClientContext oAuth2ClientContext) {
    this.sso = sso;
    this.oAuth2ClientContext = oAuth2ClientContext;
  }

  @Bean
  @ConfigurationProperties(prefix = "security.oauth2.client")
  public ClientCredentialsResourceDetails clientCredentialsResourceDetails() {
    return new ClientCredentialsResourceDetails();
  }

  @Bean
  public OAuth2RestOperations restTemplate(OAuth2ClientContext oauth2ClientContext) {
    return new OAuth2RestTemplate(clientCredentialsResourceDetails(), oauth2ClientContext);
  }

  @Bean
  public JwtAccessTokenCustomizer jwtAccessTokenCustomizer(ObjectMapper mapper) {
    return new JwtAccessTokenCustomizer(mapper);
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer config) {
    config.resourceId(sso.getResourceId());
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/v2/api-docs", "/swagger*/**", "/webjars/**").permitAll()
        .antMatchers("/ping").permitAll()
        .anyRequest().authenticated();
    http.cors();
  }
}