package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.MessageCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageDeletedEvent;
import org.jhapy.cqrs.event.i18n.MessageImportedEvent;
import org.jhapy.cqrs.event.i18n.MessageUpdatedEvent;
import org.jhapy.cqrs.query.i18n.CountAnyMatchingMessageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageByIdQuery;
import org.jhapy.dto.serviceQuery.CountChangeResult;
import org.jhapy.i18n.converter.MessageConverter;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.repository.MessageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ProcessingGroup("message-group")
public class MessageEventHandler implements HasLogger {
  private final MessageRepository repository;
  private final MessageConverter converter;
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
  public void on(MessageCreatedEvent event) throws Exception {
    Message entity = converter.toEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetMessageByIdQuery.class, query -> true, converter.asDTOWithTranslations(entity, null));

    queryUpdateEmitter.emit(
        CountAnyMatchingMessageQuery.class, query -> true, new CountChangeResult());
  }

  @EventHandler
  public void on(MessageUpdatedEvent event) throws Exception {
    Message entity = converter.toEntity(event);
    entity = repository.save(entity);
    queryUpdateEmitter.emit(
        GetMessageByIdQuery.class, query -> true, converter.asDTOWithTranslations(entity, null));
  }

  @EventHandler
  public void on(MessageImportedEvent event) throws Exception {
    List<Message> entities = converter.asEntityList(event.getMessages().values(), null);
    repository.saveAll(entities);
  }

  @EventHandler
  public void on(MessageDeletedEvent event) throws Exception {
    repository.deleteById(event.getId());
  }
}
