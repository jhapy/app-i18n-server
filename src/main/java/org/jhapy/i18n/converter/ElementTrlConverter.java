package org.jhapy.i18n.converter;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.jhapy.cqrs.event.i18n.ElementTrlCreatedEvent;
import org.jhapy.cqrs.event.i18n.ElementTrlUpdatedEvent;
import org.jhapy.dto.domain.exception.EntityNotFoundException;
import org.jhapy.dto.domain.i18n.ElementTrlDTO;
import org.jhapy.i18n.command.ElementTrlAggregate;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.repository.ElementRepository;
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
public abstract class ElementTrlConverter extends GenericMapper<ElementTrl, ElementTrlDTO> {
  public static ElementTrlConverter INSTANCE = Mappers.getMapper(ElementTrlConverter.class);
  @Autowired private ElementRepository elementRepository;

  public abstract ElementTrlCreatedEvent toElementTrlCreatedEvent(ElementTrlDTO dto);

  public abstract ElementTrlAggregate toElementTrlAggregate(ElementTrlCreatedEvent dto);

  public abstract ElementTrlAggregate toElementTrlAggregate(ElementTrlUpdatedEvent dto);

  public abstract ElementTrlUpdatedEvent toElementTrlUpdatedEvent(ElementTrlDTO dto);

  public abstract void updateAggregateFromElementTrlCreatedEvent(
      ElementTrlCreatedEvent event, @MappingTarget ElementTrlAggregate aggregate);

  public abstract void updateAggregateFromElementTrlUpdatedEvent(
      ElementTrlUpdatedEvent event, @MappingTarget ElementTrlAggregate aggregate);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "active", ignore = true)
  public abstract ElementTrl asEntity(ElementTrlCreatedEvent event);

  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "active", ignore = true)
  public abstract ElementTrl asEntity(ElementTrlUpdatedEvent event);

  public abstract ElementTrl asEntity(ElementTrlDTO dto, @Context Map<String, Object> context);

  @Mapping(target = "name", ignore = true)
  public abstract ElementTrlDTO asDTO(ElementTrl domain, @Context Map<String, Object> context);

  public Map<String, ElementTrl> asEntityMap(
      List<ElementTrlDTO> dtoList, @Context Map<String, Object> context) {
    Map<String, ElementTrl> result = new HashMap<>();
    dtoList.forEach(
        elementTrlDTO ->
            result.put(elementTrlDTO.getIso3Language(), asEntity(elementTrlDTO, context)));
    return result;
  }

  public Map<String, ElementTrlAggregate> asAggregateMapFromCreatedEvent(
      List<ElementTrlCreatedEvent> createdEventList) {
    Map<String, ElementTrlAggregate> result = new HashMap<>();
    createdEventList.forEach(
        createdEvent ->
            result.put(createdEvent.getIso3Language(), toElementTrlAggregate(createdEvent)));
    return result;
  }

  public Map<String, ElementTrlAggregate> asAggregateMapFromUpdatedEvent(
      List<ElementTrlUpdatedEvent> updatedEventList) {
    Map<String, ElementTrlAggregate> result = new HashMap<>();
    updatedEventList.forEach(
        updatedEvent ->
            result.put(updatedEvent.getIso3Language(), toElementTrlAggregate(updatedEvent)));
    return result;
  }

  public List<ElementTrlDTO> asDTOList(
      Map<String, ElementTrl> value, @Context Map<String, Object> context) {
    return value.values().stream().map(elementTrl -> asDTO(elementTrl, context)).toList();
  }

  @AfterMapping
  protected void afterConvert(
      ElementTrlDTO dto, @MappingTarget ElementTrl domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      ElementTrl domain, @MappingTarget ElementTrlDTO dto, @Context Map<String, Object> context) {
    Element parent = null;
    try {
      parent = elementRepository.getById(domain.getParentId());

    } catch (EntityNotFoundException ignored) {
    }
    if (parent != null) dto.setName(parent.getName());
  }
}
