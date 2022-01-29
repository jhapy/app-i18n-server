package org.jhapy.i18n.converter;

import org.jhapy.cqrs.event.i18n.MessageCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.MessageTrlUpdatedEvent;
import org.jhapy.cqrs.event.i18n.MessageUpdatedEvent;
import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.i18n.command.MessageAggregate;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = {RelationalDbReferenceMapper.class, MessageTrlConverter.class})
public abstract class MessageConverter extends GenericMapper<Message, MessageDTO> {
  public static MessageConverter INSTANCE = Mappers.getMapper(MessageConverter.class);

  public abstract MessageCreatedEvent toMessageCreatedEvent(MessageDTO dto);

  public abstract MessageUpdatedEvent toMessageUpdatedEvent(MessageDTO dto);

  @Mapping(target = "converter", ignore = true)
  @Mapping(target = "trlConverter", ignore = true)
  public abstract void updateAggregateFromMessageCreatedEvent(
      MessageCreatedEvent event, @MappingTarget MessageAggregate aggregate);

  @Mapping(target = "converter", ignore = true)
  @Mapping(target = "trlConverter", ignore = true)
  public abstract void updateAggregateFromMessageUpdatedEvent(
      MessageUpdatedEvent event, @MappingTarget MessageAggregate aggregate);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  public abstract Message toEntity(MessageCreatedEvent event);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  public abstract Message toEntity(MessageUpdatedEvent event);

  public abstract Message asEntity(MessageDTO dto, @Context Map<String, Object> context);

  public Map<String, MessageTrl> toMessageTrlCreatedMap(List<MessageTrlCreatedEvent> value) {
    Map<String, MessageTrl> result = new HashMap<>();
    value.forEach(
        elementTrlCreatedEvent ->
            result.put(
                elementTrlCreatedEvent.getIso3Language(),
                MessageTrlConverter.INSTANCE.asEntity(elementTrlCreatedEvent)));
    return result;
  }

  public Map<String, MessageTrl> toMessageTrlUpdatedMap(List<MessageTrlUpdatedEvent> value) {
    Map<String, MessageTrl> result = new HashMap<>();
    value.forEach(
        elementTrlCreatedEvent ->
            result.put(
                elementTrlCreatedEvent.getIso3Language(),
                MessageTrlConverter.INSTANCE.asEntity(elementTrlCreatedEvent)));
    return result;
  }

  @Mapping(target = "translations", ignore = true)
  public abstract MessageDTO asDTO(Message domain, @Context Map<String, Object> context);

  @Named(value = "withTranslation")
  public abstract MessageDTO asDTOWithTranslations(
      Message domain, @Context Map<String, Object> context);

  @AfterMapping
  protected void afterConvert(
      MessageDTO dto, @MappingTarget Message domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      Message domain, @MappingTarget MessageDTO dto, @Context Map<String, Object> context) {}
}
