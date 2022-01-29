package org.jhapy.i18n.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.jhapy.cqrs.command.AbstractEntityTranslationAggregate;
import org.jhapy.cqrs.command.i18n.CreateMessageTrlCommand;
import org.jhapy.cqrs.command.i18n.DeleteMessageTrlCommand;
import org.jhapy.cqrs.command.i18n.UpdateMessageTrlCommand;
import org.jhapy.cqrs.event.i18n.MessageTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlDeletedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlUpdatedEvent;
import org.jhapy.i18n.converter.MessageTrlConverter;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MessageTrlAggregate extends AbstractEntityTranslationAggregate {
  private String value;

  public MessageTrlAggregate(UUID id) {
    super(id);
  }

  @CommandHandler
  public void handle(CreateMessageTrlCommand command) {
    MessageTrlCreatedEvent event =
        MessageTrlConverter.INSTANCE.toMessageTrlCreatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(UpdateMessageTrlCommand command) {
    MessageTrlUpdatedEvent event =
        MessageTrlConverter.INSTANCE.toMessageTrlUpdatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(DeleteMessageTrlCommand command) {
    MessageTrlDeletedEvent event = new MessageTrlDeletedEvent(command.getId());
    AggregateLifecycle.apply(event);
  }

  @EventSourcingHandler
  public void on(MessageTrlCreatedEvent event) {
    MessageTrlConverter.INSTANCE.updateAggregateFromMessageTrlCreatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(MessageTrlUpdatedEvent event) {
    MessageTrlConverter.INSTANCE.updateAggregateFromMessageTrlUpdatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(MessageTrlDeletedEvent event) {
    markDeleted();
  }
}
