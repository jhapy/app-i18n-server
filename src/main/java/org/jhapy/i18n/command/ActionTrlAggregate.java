package org.jhapy.i18n.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.jhapy.cqrs.command.AbstractEntityTranslationAggregate;
import org.jhapy.cqrs.command.i18n.CreateActionTrlCommand;
import org.jhapy.cqrs.command.i18n.DeleteActionTrlCommand;
import org.jhapy.cqrs.command.i18n.UpdateActionTrlCommand;
import org.jhapy.cqrs.event.i18n.ActionTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionTrlDeletedEvent;
import org.jhapy.cqrs.event.i18n.ActionTrlUpdatedEvent;
import org.jhapy.i18n.converter.ActionTrlConverter;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ActionTrlAggregate extends AbstractEntityTranslationAggregate {
  private String value;

  private String tooltip;

  public ActionTrlAggregate(UUID id) {
    super(id);
  }

  @CommandHandler
  public void handle(CreateActionTrlCommand command) {
    ActionTrlCreatedEvent event =
        ActionTrlConverter.INSTANCE.toActionTrlCreatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(UpdateActionTrlCommand command) {
    ActionTrlUpdatedEvent event =
        ActionTrlConverter.INSTANCE.toActionTrlUpdatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(DeleteActionTrlCommand command) {
    ActionTrlDeletedEvent event = new ActionTrlDeletedEvent(command.getId());
    AggregateLifecycle.apply(event);
  }

  @EventSourcingHandler
  public void on(ActionTrlCreatedEvent event) {
    ActionTrlConverter.INSTANCE.updateAggregateFromActionTrlCreatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(ActionTrlUpdatedEvent event) {
    ActionTrlConverter.INSTANCE.updateAggregateFromActionTrlUpdatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(ActionTrlDeletedEvent event) {
    markDeleted();
  }
}
