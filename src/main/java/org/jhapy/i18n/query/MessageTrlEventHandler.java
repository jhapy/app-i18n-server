package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.MessageTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlDeletedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlUpdatedEvent;
import org.jhapy.cqrs.query.i18n.GetMessageByIdQuery;
import org.jhapy.i18n.converter.MessageTrlConverter;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.MessageTrlRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ProcessingGroup("message-group")
public class MessageTrlEventHandler implements HasLogger {
  private final MessageTrlRepository repository;
  private final MessageTrlConverter converter;
  private final QueryUpdateEmitter queryUpdateEmitter;

  @ExceptionHandler
  public void handleException(Exception ex) throws Exception {
    String loggerPrefix = getLoggerPrefix("handleException");
    error(
        loggerPrefix,
        ex,
        "Exception in EventHandler (ExceptionHandler): {0}:{1}",
        ex.getClass().getName(),
        ex.getMessage());
    throw ex;
  }

  @EventHandler
  public void on(MessageTrlCreatedEvent event) throws Exception {
    MessageTrl entity = converter.asEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetMessageByIdQuery.class, query -> true, converter.asDTO(entity, null));
  }

  @EventHandler
  public void on(MessageTrlUpdatedEvent event) throws Exception {
    MessageTrl entity = converter.asEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetMessageByIdQuery.class, query -> true, converter.asDTO(entity, null));
  }

  @EventHandler
  public void on(MessageTrlDeletedEvent event) throws Exception {
    repository.deleteById(event.getId());
  }
}
