package org.jhapy.i18n.converter;

import org.jhapy.cqrs.event.i18n.MessageTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlUpdatedEvent;
import org.jhapy.dto.domain.exception.EntityNotFoundException;
import org.jhapy.dto.domain.i18n.MessageTrlDTO;
import org.jhapy.i18n.command.MessageTrlAggregate;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.MessageRepository;
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
public abstract class MessageTrlConverter extends GenericMapper<MessageTrl, MessageTrlDTO> {
  public static MessageTrlConverter INSTANCE = Mappers.getMapper(MessageTrlConverter.class);
  @Autowired private MessageRepository messageRepository;

  public abstract MessageTrlCreatedEvent toMessageTrlCreatedEvent(MessageTrlDTO dto);

  public abstract MessageTrlAggregate toMessageTrlAggregate(MessageTrlCreatedEvent dto);

  public abstract MessageTrlAggregate toMessageTrlAggregate(MessageTrlUpdatedEvent dto);

  public abstract MessageTrlUpdatedEvent toMessageTrlUpdatedEvent(MessageTrlDTO dto);

  public abstract void updateAggregateFromMessageTrlCreatedEvent(
      MessageTrlCreatedEvent event, @MappingTarget MessageTrlAggregate aggregate);

  public abstract void updateAggregateFromMessageTrlUpdatedEvent(
      MessageTrlUpdatedEvent event, @MappingTarget MessageTrlAggregate aggregate);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "active", ignore = true)
  public abstract MessageTrl asEntity(MessageTrlCreatedEvent event);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "active", ignore = true)
  public abstract MessageTrl asEntity(MessageTrlUpdatedEvent event);

  public abstract MessageTrl asEntity(MessageTrlDTO dto, @Context Map<String, Object> context);

  @Mapping(target = "name", ignore = true)
  public abstract MessageTrlDTO asDTO(MessageTrl domain, @Context Map<String, Object> context);

  public Map<String, MessageTrl> asEntityMap(
      List<MessageTrlDTO> dtoList, @Context Map<String, Object> context) {
    Map<String, MessageTrl> result = new HashMap<>();
    dtoList.forEach(
        messageTrlDTO ->
            result.put(messageTrlDTO.getIso3Language(), asEntity(messageTrlDTO, context)));
    return result;
  }

  public Map<String, MessageTrlAggregate> asAggregateMapFromCreatedEvent(
      List<MessageTrlCreatedEvent> createdEventList) {
    Map<String, MessageTrlAggregate> result = new HashMap<>();
    createdEventList.forEach(
        createdEvent ->
            result.put(createdEvent.getIso3Language(), toMessageTrlAggregate(createdEvent)));
    return result;
  }

  public Map<String, MessageTrlAggregate> asAggregateMapFromUpdatedEvent(
      List<MessageTrlUpdatedEvent> updatedEventList) {
    Map<String, MessageTrlAggregate> result = new HashMap<>();
    updatedEventList.forEach(
        updatedEvent ->
            result.put(updatedEvent.getIso3Language(), toMessageTrlAggregate(updatedEvent)));
    return result;
  }

  public List<MessageTrlDTO> asDTOList(
      Map<String, MessageTrl> value, @Context Map<String, Object> context) {
    return value.values().stream().map(messageTrl -> asDTO(messageTrl, context)).toList();
  }

  @AfterMapping
  protected void afterConvert(
      MessageTrlDTO dto, @MappingTarget MessageTrl domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      MessageTrl domain, @MappingTarget MessageTrlDTO dto, @Context Map<String, Object> context) {
    Message parent = null;
    try {
      parent = messageRepository.getById(domain.getParentId());
    } catch (EntityNotFoundException ignored) {
    }
    if (parent != null) dto.setName(parent.getName());
  }
}
