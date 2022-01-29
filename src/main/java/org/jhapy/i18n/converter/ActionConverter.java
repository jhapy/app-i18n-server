package org.jhapy.i18n.converter;

import org.jhapy.cqrs.event.i18n.ActionCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionTrlUpdatedEvent;
import org.jhapy.cqrs.event.i18n.ActionUpdatedEvent;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.i18n.command.ActionAggregate;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = {RelationalDbReferenceMapper.class, ActionTrlConverter.class})
public abstract class ActionConverter extends GenericMapper<Action, ActionDTO> {
  public static ActionConverter INSTANCE = Mappers.getMapper(ActionConverter.class);

  public abstract ActionCreatedEvent toActionCreatedEvent(ActionDTO dto);

  public abstract ActionUpdatedEvent toActionUpdatedEvent(ActionDTO dto);

  @Mapping(target = "converter", ignore = true)
  @Mapping(target = "trlConverter", ignore = true)
  public abstract void updateAggregateFromActionCreatedEvent(
      ActionCreatedEvent event, @MappingTarget ActionAggregate aggregate);

  @Mapping(target = "converter", ignore = true)
  @Mapping(target = "trlConverter", ignore = true)
  public abstract void updateAggregateFromActionUpdatedEvent(
      ActionUpdatedEvent event, @MappingTarget ActionAggregate aggregate);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  public abstract Action toEntity(ActionCreatedEvent event);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  public abstract Action toEntity(ActionUpdatedEvent event);

  public abstract Action asEntity(ActionDTO dto, @Context Map<String, Object> context);

  public Map<String, ActionTrl> toActionTrlCreatedMap(List<ActionTrlCreatedEvent> value) {
    Map<String, ActionTrl> result = new HashMap<>();
    value.forEach(
        actionTrlCreatedEvent ->
            result.put(
                actionTrlCreatedEvent.getIso3Language(),
                ActionTrlConverter.INSTANCE.asEntity(actionTrlCreatedEvent)));
    return result;
  }

  public Map<String, ActionTrl> toActionTrlUpdatedMap(List<ActionTrlUpdatedEvent> value) {
    Map<String, ActionTrl> result = new HashMap<>();
    value.forEach(
        actionTrlCreatedEvent ->
            result.put(
                actionTrlCreatedEvent.getIso3Language(),
                ActionTrlConverter.INSTANCE.asEntity(actionTrlCreatedEvent)));
    return result;
  }

  @Mapping(target = "translations", ignore = true)
  public abstract ActionDTO asDTO(Action domain, @Context Map<String, Object> context);

  @Named(value = "withTranslation")
  public abstract ActionDTO asDTOWithTranslations(
      Action domain, @Context Map<String, Object> context);

  @AfterMapping
  protected void afterConvert(
      ActionDTO dto, @MappingTarget Action domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      Action domain, @MappingTarget ActionDTO dto, @Context Map<String, Object> context) {}
}
