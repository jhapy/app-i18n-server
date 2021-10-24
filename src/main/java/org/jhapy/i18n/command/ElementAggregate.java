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
import org.jhapy.cqrs.command.i18n.CreateElementCommand;
import org.jhapy.cqrs.command.i18n.DeleteElementCommand;
import org.jhapy.cqrs.command.i18n.UpdateElementCommand;
import org.jhapy.cqrs.event.i18n.ElementCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementDeletedEvent;
import org.jhapy.cqrs.event.i18n.ElementUpdatedEvent;
import org.jhapy.i18n.converter.ElementConverter;
import org.jhapy.i18n.converter.ElementTrlConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ElementAggregate extends AbstractBaseAggregate {
  private String name;

  private String category;

  private Boolean isTranslated = Boolean.FALSE;

  private transient ElementConverter converter;
  private transient ElementTrlConverter trlConverter;

  @AggregateMember private Map<String, ElementTrlAggregate> translations;

  @CommandHandler
  public ElementAggregate(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          CreateElementCommand command,
      @Autowired ElementConverter elementConverter,
      @Autowired ElementTrlConverter elementTrlConverter) {
    this.converter = elementConverter;
    this.trlConverter = elementTrlConverter;

    if (StringUtils.isBlank(command.getEntity().getName())) {
      throw new IllegalArgumentException("Entity name can not be empty");
    }

    if (command.getEntity().getTranslations() != null)
      command
          .getEntity()
          .getTranslations()
          .forEach(
              elementTrlDTO -> {
                if (StringUtils.isBlank(elementTrlDTO.getValue())) {
                  throw new IllegalArgumentException("Entity Trl Value can not be empty");
                }
                if (StringUtils.isBlank(elementTrlDTO.getIso3Language())) {
                  throw new IllegalArgumentException("Entity Trl Iso3 Langage can not be empty");
                }
                elementTrlDTO.setId(UUID.randomUUID());
                elementTrlDTO.setParentId(command.getId());
              });
    ElementCreatedEvent event = converter.toElementCreatedEvent(command.getEntity());
    event.setId(command.getId());
    AggregateLifecycle.apply(event);
  }

  @Autowired
  public void setConverter(ElementConverter converter) {
    this.converter = converter;
  }

  @Autowired
  public void setTrlConverter(ElementTrlConverter trlConverter) {
    this.trlConverter = trlConverter;
  }

  @CommandHandler
  public void handle(UpdateElementCommand command) {
    if (command.getEntity().getTranslations() != null)
      command
          .getEntity()
          .getTranslations()
          .forEach(elementTrlDTO -> elementTrlDTO.setParentId(command.getId()));

    ElementUpdatedEvent event =
        ElementConverter.INSTANCE.toElementUpdatedEvent(command.getEntity());
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(DeleteElementCommand command) {
    if (command.getId() == null) {
      throw new IllegalArgumentException("ID can not be empty");
    }
    ElementDeletedEvent event = new ElementDeletedEvent(command.getId());
    AggregateLifecycle.apply(event);
  }

  @EventSourcingHandler
  public void on(ElementCreatedEvent event) {
    converter.updateAggregateFromElementCreatedEvent(event, this);
    event
        .getTranslations()
        .forEach(
            elementTrlCreatedEvent -> {
              ElementTrlAggregate elementTrlAggregate =
                  new ElementTrlAggregate(elementTrlCreatedEvent.getId());
              trlConverter.updateAggregateFromElementTrlCreatedEvent(
                  elementTrlCreatedEvent, elementTrlAggregate);
              translations.put(elementTrlCreatedEvent.getIso3Language(), elementTrlAggregate);
            });
  }

  @EventSourcingHandler
  public void on(ElementUpdatedEvent event) {
    converter.updateAggregateFromElementUpdatedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(ElementDeletedEvent event) {
    markDeleted();
  }
}