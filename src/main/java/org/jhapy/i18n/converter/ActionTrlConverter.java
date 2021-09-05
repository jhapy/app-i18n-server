package org.jhapy.i18n.converter;

import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.service.ActionService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = RelationalDbReferenceMapper.class)
public abstract class ActionTrlConverter extends GenericMapper<ActionTrl, ActionTrlDTO> {
  @Autowired private ActionService actionService;

  public abstract ActionTrl asEntity(ActionTrlDTO dto, @Context Map<String, Object> context);

  public abstract ActionTrlDTO asDTO(ActionTrl domain, @Context Map<String, Object> context);

  public List<ActionTrlDTO> asDTOList(
      Iterable<ActionTrl> entityList, @Context Map<String, Object> context) {
    if (entityList == null) {
      return null;
    }
    long id = 1;
    var list = new ArrayList<ActionTrlDTO>();
    for (ActionTrl actionTrl : entityList) {
      var actionTrlDTO = asDTO(actionTrl, context);
      actionTrlDTO.setId(id++);
      list.add(actionTrlDTO);
    }

    return list;
  }

  @AfterMapping
  protected void afterConvert(
      ActionTrlDTO dto, @MappingTarget ActionTrl domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      ActionTrl domain, @MappingTarget ActionTrlDTO dto, @Context Map<String, Object> context) {
    if (domain.getRelatedEntityId() != null && StringUtils.isBlank(domain.getName()))
      dto.setName(actionService.load(domain.getRelatedEntityId()).getName());
  }
}