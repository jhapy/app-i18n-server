package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.ActionCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionDeletedEvent;
import org.jhapy.cqrs.event.i18n.ActionImportedEvent;
import org.jhapy.cqrs.event.i18n.ActionUpdatedEvent;
import org.jhapy.cqrs.query.i18n.CountAnyMatchingActionQuery;
import org.jhapy.cqrs.query.i18n.GetActionByIdQuery;
import org.jhapy.dto.serviceQuery.CountChangeResult;
import org.jhapy.i18n.converter.ActionConverter;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.repository.ActionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ProcessingGroup("action-group")
public class ActionEventHandler implements HasLogger {
  private final ActionRepository repository;
  private final ActionConverter converter;
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
  public void on(ActionCreatedEvent event) throws Exception {
    Action entity = converter.toEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetActionByIdQuery.class, query -> true, converter.asDTOWithTranslations(entity, null));

    queryUpdateEmitter.emit(
        CountAnyMatchingActionQuery.class, query -> true, new CountChangeResult());
  }

  @EventHandler
  public void on(ActionUpdatedEvent event) throws Exception {
    Action entity = converter.toEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetActionByIdQuery.class, query -> true, converter.asDTOWithTranslations(entity, null));
  }

  @EventHandler
  public void on(ActionImportedEvent event) throws Exception {
    List<Action> entities = converter.asEntityList(event.getActions().values(), null);
    repository.saveAll(entities);
  }

  @EventHandler
  public void on(ActionDeletedEvent event) throws Exception {
    repository.deleteById(event.getId());
  }
}
