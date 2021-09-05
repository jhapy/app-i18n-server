package org.jhapy.i18n.converter;

import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.i18n.ElementTrlDTO;
import org.jhapy.dto.domain.i18n.MessageTrlDTO;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.MessageService;
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
public abstract class MessageTrlConverter extends GenericMapper<MessageTrl, MessageTrlDTO> {
  @Autowired private MessageService messageService;

  public abstract MessageTrl asEntity(MessageTrlDTO dto, @Context Map<String, Object> context);

  public abstract MessageTrlDTO asDTO(MessageTrl domain, @Context Map<String, Object> context);

  public List<MessageTrlDTO> asDTOList(
      Iterable<MessageTrl> entityList, @Context Map<String, Object> context) {
    if (entityList == null) {
      return null;
    }
    long id = 1;
    var list = new ArrayList<MessageTrlDTO>();
    for (MessageTrl messageTrl : entityList) {
      var messageTrlDTO = asDTO(messageTrl, context);
      messageTrlDTO.setId(id++);
      list.add(messageTrlDTO);
    }

    return list;
  }

  @AfterMapping
  protected void afterConvert(
      MessageTrlDTO dto, @MappingTarget MessageTrl domain, @Context Map<String, Object> context) {}

  @AfterMapping
  protected void afterConvert(
      MessageTrl domain, @MappingTarget MessageTrlDTO dto, @Context Map<String, Object> context) {
    if (domain.getRelatedEntityId() != null && StringUtils.isBlank(domain.getName()))
      dto.setName(messageService.load(domain.getRelatedEntityId()).getName());
  }
}