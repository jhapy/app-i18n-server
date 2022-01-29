package org.jhapy.i18n.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.jhapy.cqrs.command.AbstractEntityTranslationAggregate;
import org.jhapy.cqrs.command.i18n.CreateElementTrlCommand;
import org.jhapy.cqrs.command.i18n.DeleteElementTrlCommand;
import org.jhapy.cqrs.command.i18n.UpdateElementTrlCommand;
import org.jhapy.cqrs.event.i18n.ElementTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementTrlDeletedEvent;
import org.jhapy.cqrs.event.i18n.ElementTrlUpdatedEvent;
import org.jhapy.i18n.converter.ElementTrlConverter;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ElementTrlAggregate extends AbstractEntityTranslationAggregate {
  private String value;

  private String tooltip;

  public ElementTrlAggregate(UUID id) {
    super(id);
  }

  @CommandHandler
  public void handle(CreateElementTrlCommand command) {
    ElementTrlCreatedEvent event =
        ElementTrlConverter.INSTANCE.toElementTrlCreatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(UpdateElementTrlCommand command) {
    ElementTrlUpdatedEvent event =
        ElementTrlConverter.INSTANCE.toElementTrlUpdatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(DeleteElementTrlCommand command) {
    ElementTrlDeletedEvent event = new ElementTrlDeletedEvent(command.getId());
    AggregateLifecycle.apply(event);
  }

  @EventSourcingHandler
  public void on(ElementTrlCreatedEvent event) {
    ElementTrlConverter.INSTANCE.updateAggregateFromElementTrlCreatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(ElementTrlUpdatedEvent event) {
    ElementTrlConverter.INSTANCE.updateAggregateFromElementTrlUpdatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(ElementTrlDeletedEvent event) {
    markDeleted();
  }
}
