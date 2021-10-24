package org.jhapy.i18n.command;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.jhapy.cqrs.event.i18n.ElementCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementUpdatedEvent;
import org.jhapy.i18n.domain.ElementLookup;
import org.jhapy.i18n.repository.ElementLookupRepository;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("element-group")
@RequiredArgsConstructor
public class ElementLookupEventsHandler {

  private final ElementLookupRepository repository;

  @EventHandler
  public void on(ElementCreatedEvent event) {
    ElementLookup entity = new ElementLookup(event.getId(), event.getName());
    repository.save(entity);
  }

  @EventHandler
  public void on(ElementUpdatedEvent event) {
    ElementLookup entity = repository.getById(event.getId());
    entity.setName(event.getName());
    repository.save(entity);
  }

  @ResetHandler
  public void reset() {
    repository.deleteAllInBatch();
  }
}