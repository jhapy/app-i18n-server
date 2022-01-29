package org.jhapy.i18n.converter;

import org.jhapy.cqrs.event.i18n.ElementCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementTrlUpdatedEvent;
import org.jhapy.cqrs.event.i18n.ElementUpdatedEvent;
import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.i18n.command.ElementAggregate;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = {RelationalDbReferenceMapper.class, ElementTrlConverter.class})
public abstract class ElementConverter extends GenericMapper<Element, ElementDTO> {
  public static ElementConverter INSTANCE = Mappers.getMapper(ElementConverter.class);

  public abstract ElementCreatedEvent toElementCreatedEvent(ElementDTO dto);

  public abstract ElementUpdatedEvent toElementUpdatedEvent(ElementDTO dto);

  @Mapping(target = "converter", ignore = true)
  @Mapping(target = "trlConverter", ignore = true)
  public abstract void updateAggregateFromElementCreatedEvent(
      ElementCreatedEvent event, @MappingTarget ElementAggregate aggregate);

  @Mapping(target = "converter", ignore = true)
  @Mapping(target = "trlConverter", ignore = true)
  public abstract void updateAggregateFromElementUpdatedEvent(
      ElementUpdatedEvent event, @MappingTarget ElementAggregate aggregate);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  public abstract Element toEntity(ElementCreatedEvent event);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  public abstract Element toEntity(ElementUpdatedEvent event);

  public abstract Element asEntity(ElementDTO dto, @Context Map<String, Object> context);

  public Map<String, ElementTrl> toElementTrlCreatedMap(List<ElementTrlCreatedEvent> value) {
    Map<String, ElementTrl> result = new HashMap<>();
    value.forEach(
        elementTrlCreatedEvent ->
            result.put(
                elementTrlCreatedEvent.getIso3Language(),
                ElementTrlConverter.INSTANCE.asEntity(elementTrlCreatedEvent)));
    return result;
  }

  public Map<String, ElementTrl> toElementTrlUpdatedMap(List<ElementTrlUpdatedEvent> value) {
    Map<String, ElementTrl> result = new HashMap<>();
    value.forEach(
        elementTrlCreatedEvent ->
            result.put(
                elementTrlCreatedEvent.getIso3Language(),
                ElementTrlConverter.INSTANCE.asEntity(elementTrlCreatedEvent)));
    return result;
  }

  @Mapping(target = "translations", ignore = true)
  public abstract ElementDTO asDTO(Element domain, @Context Map<String, Object> context);

  @Named(value = "withTranslation")
  public abstract ElementDTO asDTOWithTranslations(
      Element domain, @Context Map<String, Object> context);

  @AfterMapping
  protected void afterConvert(
      ElementDTO dto, @MappingTarget Element domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      Element domain, @MappingTarget ElementDTO dto, @Context Map<String, Object> context) {}
}
