package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.ActionTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionTrlUpdatedEvent;
import org.jhapy.cqrs.query.i18n.GetActionByIdQuery;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.repository.ActionTrlRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ProcessingGroup("action-group")
public class ActionTrlEventHandler implements HasLogger {
  private final ActionTrlRepository repository;
  private final ActionTrlConverter converter;
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
  public void on(ActionTrlCreatedEvent event) throws Exception {
    ActionTrl entity = converter.asEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(GetActionByIdQuery.class, query -> true, converter.asDTO(entity, null));
  }

  @EventHandler
  public void on(ActionTrlUpdatedEvent event) throws Exception {
    ActionTrl entity = converter.asEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(GetActionByIdQuery.class, query -> true, converter.asDTO(entity, null));
  }
}