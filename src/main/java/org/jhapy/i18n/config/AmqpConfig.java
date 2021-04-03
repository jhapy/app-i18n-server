package org.jhapy.i18n.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 27/03/2021
 */
@Configuration
public class AmqpConfig {
  @Bean
  public FanoutExchange elementUpdate() {
    return new FanoutExchange("i18n.elementUpdate");
  }

  @Bean
  public FanoutExchange elementTrlUpdate() {
    return new FanoutExchange("i18n.elementTrlUpdate");
  }
  @Bean
  public FanoutExchange actionUpdate() {
    return new FanoutExchange("i18n.actionUpdate");
  }
  @Bean
  public FanoutExchange actionTrlUpdate() {
    return new FanoutExchange("i18n.actionTrlUpdate");
  }

  @Bean public FanoutExchange messageUpdate() {
    return new FanoutExchange("i18n.messageUpdate");
  }
  @Bean
  public FanoutExchange messageTrlUpdate() {
    return new FanoutExchange("i18n.messageTrlUpdate");
  }
}
