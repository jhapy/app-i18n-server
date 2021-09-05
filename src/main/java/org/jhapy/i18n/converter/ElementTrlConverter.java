package org.jhapy.i18n.converter;

import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.dto.domain.i18n.ElementTrlDTO;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.service.ElementService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = RelationalDbReferenceMapper.class)
public abstract class ElementTrlConverter extends GenericMapper<ElementTrl, ElementTrlDTO> {
  @Autowired private ElementService elementService;

  public abstract ElementTrl asEntity(ElementTrlDTO dto, @Context Map<String, Object> context);

  public abstract ElementTrlDTO asDTO(ElementTrl domain, @Context Map<String, Object> context);

  public List<ElementTrlDTO> asDTOList(
      Iterable<ElementTrl> entityList, @Context Map<String, Object> context) {
    if (entityList == null) {
      return null;
    }
    long id = 1;
    var list = new ArrayList<ElementTrlDTO>();
    for (ElementTrl elementTrl : entityList) {
      var elementTrlDTO = asDTO(elementTrl, context);
      elementTrlDTO.setId(id++);
      list.add(elementTrlDTO);
    }

    return list;
  }

  @AfterMapping
  protected void afterConvert(
      ElementTrlDTO dto, @MappingTarget ElementTrl domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      ElementTrl domain, @MappingTarget ElementTrlDTO dto, @Context Map<String, Object> context) {
    if (domain.getRelatedEntityId() != null && StringUtils.isBlank(domain.getName()))
      dto.setName(elementService.load(domain.getRelatedEntityId()).getName());
  }
}