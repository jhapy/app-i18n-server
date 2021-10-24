package org.jhapy.i18n.config;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.jhapy.i18n.command.interceptor.CreateOrUpdateActionCommandInterceptor;
import org.jhapy.i18n.command.interceptor.CreateOrUpdateElementCommandInterceptor;
import org.jhapy.i18n.command.interceptor.CreateOrUpdateMessageCommandInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

  @Bean
  public LoggingInterceptor<Message<?>> loggingInterceptor() {
    return new LoggingInterceptor<>();
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      CommandBus commandBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
    commandBus.registerDispatchInterceptor(loggingInterceptor);
    commandBus.registerHandlerInterceptor(loggingInterceptor);
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      EventBus eventBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
    eventBus.registerDispatchInterceptor(loggingInterceptor);
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      EventProcessingConfigurer eventProcessingConfigurer,
      LoggingInterceptor<Message<?>> loggingInterceptor) {
    eventProcessingConfigurer.registerDefaultHandlerInterceptor(
        (config, processorName) -> loggingInterceptor);
  }

  @Autowired
  public void configureLoggingInterceptorFor(
      QueryBus queryBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
    queryBus.registerDispatchInterceptor(loggingInterceptor);
    queryBus.registerHandlerInterceptor(loggingInterceptor);
  }

  @Autowired
  public void configureErrorHandlers(EventProcessingConfigurer configurer) {
    configurer.registerListenerInvocationErrorHandler(
        "element-group", configuration -> PropagatingErrorHandler.instance());
    configurer.registerListenerInvocationErrorHandler(
        "action-group", configuration -> PropagatingErrorHandler.instance());
    configurer.registerListenerInvocationErrorHandler(
        "message-group", configuration -> PropagatingErrorHandler.instance());
  }

  @Autowired
  public void registerCreateOrUpdateActionCommandInterceptor(
      CreateOrUpdateActionCommandInterceptor createOrUpdateActionCommandInterceptor,
      CommandBus commandBus) {
    commandBus.registerDispatchInterceptor(createOrUpdateActionCommandInterceptor);
  }

  @Autowired
  public void registerCreateOrUpdateElementCommandInterceptor(
      CreateOrUpdateElementCommandInterceptor createOrUpdateElementCommandInterceptor,
      CommandBus commandBus) {
    commandBus.registerDispatchInterceptor(createOrUpdateElementCommandInterceptor);
  }

  @Autowired
  public void registerCreateOrUpdateMessageCommandInterceptor(
      CreateOrUpdateMessageCommandInterceptor createOrUpdateMessageCommandInterceptor,
      CommandBus commandBus) {
    commandBus.registerDispatchInterceptor(createOrUpdateMessageCommandInterceptor);
  }
}