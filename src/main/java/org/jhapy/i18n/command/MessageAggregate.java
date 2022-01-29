package org.jhapy.i18n.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import org.jhapy.cqrs.command.AbstractBaseAggregate;
import org.jhapy.cqrs.command.i18n.CreateMessageCommand;
import org.jhapy.cqrs.command.i18n.DeleteMessageCommand;
import org.jhapy.cqrs.command.i18n.UpdateMessageCommand;
import org.jhapy.cqrs.event.i18n.MessageCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageDeletedEvent;
import org.jhapy.cqrs.event.i18n.MessageUpdatedEvent;
import org.jhapy.i18n.converter.MessageConverter;
import org.jhapy.i18n.converter.MessageTrlConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MessageAggregate extends AbstractBaseAggregate {
  private String name;

  private String category;

  private Boolean translated = Boolean.FALSE;

  private transient MessageConverter converter;
  private transient MessageTrlConverter trlConverter;

  @AggregateMember private Map<String, MessageTrlAggregate> translations;

  @CommandHandler
  public MessageAggregate(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          CreateMessageCommand command,
      @Autowired MessageConverter messageConverter,
      @Autowired MessageTrlConverter messageTrlConverter) {
    this.converter = messageConverter;
    this.trlConverter = messageTrlConverter;

    if (StringUtils.isBlank(command.getEntity().getName())) {
      throw new IllegalArgumentException("Entity name can not be empty");
    }

    command
        .getEntity()
        .getTranslations()
        .forEach(
            messageTrlDTO -> {
              if (StringUtils.isBlank(messageTrlDTO.getValue())) {
                throw new IllegalArgumentException("Entity Trl Value can not be empty");
              }
              if (StringUtils.isBlank(messageTrlDTO.getIso3Language())) {
                throw new IllegalArgumentException("Entity Trl Iso3 Language can not be empty");
              }
              messageTrlDTO.setId(UUID.randomUUID());
              messageTrlDTO.setParentId(command.getId());
            });
    MessageCreatedEvent event = converter.toMessageCreatedEvent(command.getEntity());
    event.setId(command.getId());
    AggregateLifecycle.apply(event);
  }

  @Autowired
  public void setConverter(MessageConverter converter) {
    this.converter = converter;
  }

  @Autowired
  public void setTrlConverter(MessageTrlConverter trlConverter) {
    this.trlConverter = trlConverter;
  }

  @CommandHandler
  public void handle(UpdateMessageCommand command) {
      command
          .getEntity()
          .getTranslations()
          .forEach(messageTrlDTO -> messageTrlDTO.setParentId(command.getId()));

    MessageUpdatedEvent event = converter.toMessageUpdatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(DeleteMessageCommand command) {
    MessageDeletedEvent event = new MessageDeletedEvent(command.getId());
    AggregateLifecycle.apply(event);
  }

  @EventSourcingHandler
  public void on(MessageCreatedEvent event) {
    converter.updateAggregateFromMessageCreatedEvent(event, this);
    event
        .getTranslations()
        .forEach(
            messageTrlCreatedEvent -> {
              MessageTrlAggregate messageTrlAggregate =
                  new MessageTrlAggregate(messageTrlCreatedEvent.getId());
              trlConverter.updateAggregateFromMessageTrlCreatedEvent(
                  messageTrlCreatedEvent, messageTrlAggregate);
              translations.put(messageTrlCreatedEvent.getIso3Language(), messageTrlAggregate);
            });
  }

  @EventSourcingHandler
  public void on(MessageUpdatedEvent event) {
    converter.updateAggregateFromMessageUpdatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(MessageDeletedEvent event) {
    markDeleted();
  }
}
