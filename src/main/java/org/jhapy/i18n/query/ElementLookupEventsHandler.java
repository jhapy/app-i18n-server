package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.event.i18n.ElementCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementDeletedEvent;
import org.jhapy.cqrs.event.i18n.ElementUpdatedEvent;
import org.jhapy.i18n.domain.ElementLookup;
import org.jhapy.i18n.repository.ElementLookupRepository;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("element-group")
@RequiredArgsConstructor
public class ElementLookupEventsHandler implements HasLogger {

  private final ElementLookupRepository repository;

  @EventHandler
  public void on(ElementCreatedEvent event) {
    String loggerPrefix = getLoggerPrefix("onElementCreatedEvent");
    debug(loggerPrefix, "ElementLookup, ID = {0}", event.getId());
    ElementLookup entity = new ElementLookup(event.getId(), event.getName());
    repository.save(entity);
  }

  @EventHandler
  public void on(ElementUpdatedEvent event) {
    ElementLookup entity = repository.getById(event.getId());
    entity.setName(event.getName());
    repository.save(entity);
  }

  @EventHandler
  public void on(ElementDeletedEvent event) {
    repository.deleteById(event.getId());
  }

  @ResetHandler
  public void reset() {
    repository.deleteAllInBatch();
  }
}
