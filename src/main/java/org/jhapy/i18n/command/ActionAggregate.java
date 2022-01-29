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
import org.jhapy.cqrs.command.i18n.CreateActionCommand;
import org.jhapy.cqrs.command.i18n.DeleteActionCommand;
import org.jhapy.cqrs.command.i18n.UpdateActionCommand;
import org.jhapy.cqrs.event.i18n.ActionCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionDeletedEvent;
import org.jhapy.cqrs.event.i18n.ActionUpdatedEvent;
import org.jhapy.i18n.converter.ActionConverter;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ActionAggregate extends AbstractBaseAggregate {
  private String name;

  private String category;

  private Boolean translated = Boolean.FALSE;

  private transient ActionConverter converter;
  private transient ActionTrlConverter trlConverter;

  @AggregateMember private Map<String, ActionTrlAggregate> translations;

  @CommandHandler
  public ActionAggregate(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          CreateActionCommand command,
      @Autowired ActionConverter actionConverter,
      @Autowired ActionTrlConverter actionTrlConverter) {
    this.converter = actionConverter;
    this.trlConverter = actionTrlConverter;

    if (StringUtils.isBlank(command.getEntity().getName())) {
      throw new IllegalArgumentException("Entity name can not be empty");
    }

    command
        .getEntity()
        .getTranslations()
        .forEach(
            actionTrlDTO -> {
              if (StringUtils.isBlank(actionTrlDTO.getValue())) {
                throw new IllegalArgumentException("Entity Trl Value can not be empty");
              }
              if (StringUtils.isBlank(actionTrlDTO.getIso3Language())) {
                throw new IllegalArgumentException("Entity Trl Iso3 Langage can not be empty");
              }
              actionTrlDTO.setId(UUID.randomUUID());
              actionTrlDTO.setParentId(command.getId());
            });
    ActionCreatedEvent event = converter.toActionCreatedEvent(command.getEntity());
    event.setId(command.getId());
    AggregateLifecycle.apply(event);
  }

  @Autowired
  public void setConverter(ActionConverter converter) {
    this.converter = converter;
  }

  @Autowired
  public void setTrlConverter(ActionTrlConverter trlConverter) {
    this.trlConverter = trlConverter;
  }

  @CommandHandler
  public void handle(UpdateActionCommand command) {
    command
        .getEntity()
        .getTranslations()
        .forEach(actionTrlDTO -> actionTrlDTO.setParentId(command.getId()));

    ActionUpdatedEvent event = converter.toActionUpdatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(DeleteActionCommand command) {
    ActionDeletedEvent event = new ActionDeletedEvent(command.getId());
    AggregateLifecycle.apply(event);
  }

  @EventSourcingHandler
  public void on(ActionCreatedEvent event) {
    converter.updateAggregateFromActionCreatedEvent(event, this);
    event
        .getTranslations()
        .forEach(
            actionTrlCreatedEvent -> {
              ActionTrlAggregate actionTrlAggregate =
                  new ActionTrlAggregate(actionTrlCreatedEvent.getId());
              trlConverter.updateAggregateFromActionTrlCreatedEvent(
                  actionTrlCreatedEvent, actionTrlAggregate);
              translations.put(actionTrlCreatedEvent.getIso3Language(), actionTrlAggregate);
            });
  }

  @EventSourcingHandler
  public void on(ActionUpdatedEvent event) {
    converter.updateAggregateFromActionUpdatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(ActionDeletedEvent event) {
    markDeleted();
  }
}
