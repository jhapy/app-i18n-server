package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.ElementCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementDeletedEvent;
import org.jhapy.cqrs.event.i18n.ElementImportedEvent;
import org.jhapy.cqrs.query.i18n.GetElementByIdQuery;
import org.jhapy.i18n.converter.ElementConverter;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.repository.ElementRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ProcessingGroup("element-group")
public class ElementEventHandler implements HasLogger {
  private final ElementRepository repository;
  private final ElementConverter converter;
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
  public void on(ElementCreatedEvent event) throws Exception {
    Element entity = converter.toEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetElementByIdQuery.class, query -> true, converter.asDTOWithTranslations(entity, null));
  }

  @EventHandler
  public void on(ElementImportedEvent event) throws Exception {
    List<Element> entities = converter.asEntityList(event.getElements().values(), null);
    repository.saveAll(entities);
  }

  @EventHandler
  public void on(ElementDeletedEvent event) throws Exception {
    repository.deleteById(event.getId());
  }
}