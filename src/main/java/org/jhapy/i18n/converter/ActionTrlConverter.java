package org.jhapy.i18n.converter;

import org.jhapy.cqrs.event.i18n.ActionTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ActionTrlUpdatedEvent;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.i18n.command.ActionTrlAggregate;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.repository.ActionRepository;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = RelationalDbReferenceMapper.class)
public abstract class ActionTrlConverter extends GenericMapper<ActionTrl, ActionTrlDTO> {
  public static ActionTrlConverter INSTANCE = Mappers.getMapper(ActionTrlConverter.class);
  @Autowired private ActionRepository actionRepository;

  public abstract ActionTrlCreatedEvent toActionTrlCreatedEvent(ActionTrlDTO dto);

  public abstract ActionTrlAggregate toActionTrlAggregate(ActionTrlCreatedEvent dto);

  public abstract ActionTrlAggregate toActionTrlAggregate(ActionTrlUpdatedEvent dto);

  public abstract ActionTrlUpdatedEvent toActionTrlUpdatedEvent(ActionTrlDTO dto);

  public abstract void updateAggregateFromActionTrlCreatedEvent(
      ActionTrlCreatedEvent event, @MappingTarget ActionTrlAggregate aggregate);

  public abstract void updateAggregateFromActionTrlUpdatedEvent(
      ActionTrlUpdatedEvent event, @MappingTarget ActionTrlAggregate aggregate);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "isActive", ignore = true)
  public abstract ActionTrl asEntity(ActionTrlCreatedEvent event);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "isActive", ignore = true)
  public abstract ActionTrl asEntity(ActionTrlUpdatedEvent event);

  public abstract ActionTrl asEntity(ActionTrlDTO dto, @Context Map<String, Object> context);

  @Mapping(target = "temporaryId", ignore = true)
  @Mapping(target = "name", ignore = true)
  public abstract ActionTrlDTO asDTO(ActionTrl domain, @Context Map<String, Object> context);

  public Map<String, ActionTrl> asEntityMap(
      List<ActionTrlDTO> dtoList, @Context Map<String, Object> context) {
    Map<String, ActionTrl> result = new HashMap<>();
    dtoList.forEach(
        actionTrlDTO ->
            result.put(actionTrlDTO.getIso3Language(), asEntity(actionTrlDTO, context)));
    return result;
  }

  public Map<String, ActionTrlAggregate> asAggregateMapFromCreatedEvent(
      List<ActionTrlCreatedEvent> createdEventList) {
    Map<String, ActionTrlAggregate> result = new HashMap<>();
    createdEventList.forEach(
        createdEvent ->
            result.put(createdEvent.getIso3Language(), toActionTrlAggregate(createdEvent)));
    return result;
  }

  public Map<String, ActionTrlAggregate> asAggregateMapFromUpdatedEvent(
      List<ActionTrlUpdatedEvent> updatedEventList) {
    Map<String, ActionTrlAggregate> result = new HashMap<>();
    updatedEventList.forEach(
        updatedEvent ->
            result.put(updatedEvent.getIso3Language(), toActionTrlAggregate(updatedEvent)));
    return result;
  }

  public List<ActionTrlDTO> asDTOList(
      Map<String, ActionTrl> value, @Context Map<String, Object> context) {
    return value.values().stream().map(actionTrl -> asDTO(actionTrl, context)).toList();
  }

  @AfterMapping
  protected void afterConvert(
      ActionTrlDTO dto, @MappingTarget ActionTrl domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      ActionTrl domain, @MappingTarget ActionTrlDTO dto, @Context Map<String, Object> context) {
    dto.setName(actionRepository.getById(domain.getParentId()).getName());
  }
}