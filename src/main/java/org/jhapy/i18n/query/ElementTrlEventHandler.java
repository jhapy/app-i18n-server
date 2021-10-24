package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.ElementTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementTrlUpdatedEvent;
import org.jhapy.cqrs.query.i18n.GetElementByIdQuery;
import org.jhapy.i18n.converter.ElementTrlConverter;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.repository.ElementTrlRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ProcessingGroup("element-group")
public class ElementTrlEventHandler implements HasLogger {
  private final ElementTrlRepository repository;
  private final ElementTrlConverter converter;
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
  public void on(ElementTrlCreatedEvent event) throws Exception {
    ElementTrl entity = converter.asEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetElementByIdQuery.class, query -> true, converter.asDTO(entity, null));
  }

  @EventHandler
  public void on(ElementTrlUpdatedEvent event) throws Exception {
    ElementTrl entity = converter.asEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetElementByIdQuery.class, query -> true, converter.asDTO(entity, null));
  }
}