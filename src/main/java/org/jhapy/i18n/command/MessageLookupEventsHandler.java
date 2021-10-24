package org.jhapy.i18n.command;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.jhapy.cqrs.event.i18n.MessageCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageUpdatedEvent;
import org.jhapy.i18n.domain.MessageLookup;
import org.jhapy.i18n.repository.MessageLookupRepository;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("message-group")
@RequiredArgsConstructor
public class MessageLookupEventsHandler {

  private final MessageLookupRepository repository;

  @EventHandler
  public void on(MessageCreatedEvent event) {
    MessageLookup entity = new MessageLookup(event.getId(), event.getName());
    repository.save(entity);
  }

  @EventHandler
  public void on(MessageUpdatedEvent event) {
    MessageLookup entity = repository.getById(event.getId());
    entity.setName(event.getName());
    repository.save(entity);
  }

  @ResetHandler
  public void reset() {
    repository.deleteAllInBatch();
  }
}