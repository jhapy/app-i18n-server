package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.jhapy.cqrs.event.i18n.ActionCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionDeletedEvent;
import org.jhapy.cqrs.event.i18n.ActionUpdatedEvent;
import org.jhapy.i18n.domain.ActionLookup;
import org.jhapy.i18n.repository.ActionLookupRepository;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("action-group")
@RequiredArgsConstructor
public class ActionLookupEventsHandler {

  private final ActionLookupRepository repository;

  @EventHandler
  public void on(ActionCreatedEvent event) {
    ActionLookup entity = new ActionLookup(event.getId(), event.getName());
    repository.save(entity);
  }

  @EventHandler
  public void on(ActionUpdatedEvent event) {
    ActionLookup entity = repository.getById(event.getId());
    entity.setName(event.getName());
    repository.save(entity);
  }

  @EventHandler
  public void on(ActionDeletedEvent event) {
    repository.deleteById(event.getId());
  }

  @ResetHandler
  public void reset() {
    repository.deleteAllInBatch();
  }
}
