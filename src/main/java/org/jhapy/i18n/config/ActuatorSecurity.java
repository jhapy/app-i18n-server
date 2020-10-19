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

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.cloud.netflix.hystrix.HystrixStreamEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//@EnableWebSecurity
public class ActuatorSecurity {

  private final Environment env;

  public ActuatorSecurity(Environment env) {
    this.env = env;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /*
  @Bean
  public UserDetailsService actuatorUserDetailsService() throws Exception {
    UserBuilder users = User.builder().passwordEncoder(s -> passwordEncoder().encode(s));
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(users.username(env.getProperty("spring.security.user.name")).password(
        env.getProperty("spring.security.user.password"))
        .roles(env.getProperty("spring.security.user.roles")).build());
    return manager;
  }
*/
  //@Configuration
  @Order(1)
  public static class MonitoringWebSecurityConfigurationAdapter extends
      WebSecurityConfigurerAdapter {

    /*
        private final UserDetailsService v3UserDetailsService;

        public MonitoringWebSecurityConfigurationAdapter(
            @Qualifier("actuatorUserDetailsService") UserDetailsService v3UserDetailsService) {
          this.v3UserDetailsService = v3UserDetailsService;
        }
    */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
    }

    /*
        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
          auth
              .userDetailsService(v3UserDetailsService);
        }
    */
    protected void configure(HttpSecurity http) throws Exception {
      http.antMatcher("/actuator/**")
          .authorizeRequests(authorize ->
              authorize
                  .requestMatchers(EndpointRequest.to(HealthEndpoint.class), EndpointRequest.to(
                      HystrixStreamEndpoint.class)).permitAll()
                  .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ROLE_MONITORING")
                  .anyRequest().permitAll()
          ).httpBasic(Customizer.withDefaults());
    }
  }
}