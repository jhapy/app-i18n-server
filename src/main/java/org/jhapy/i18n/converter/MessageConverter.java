package org.jhapy.i18n.converter;

import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.MessageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = {RelationalDbReferenceMapper.class, MessageTrlConverter.class})
public abstract class MessageConverter extends GenericMapper<Message, MessageDTO> {
  @Autowired private MessageService messageService;

  @Mapping(target = "translations", ignore = true)
  public abstract Message asEntity(MessageDTO dto, @Context Map<String, Object> context);

  @Mapping(target = "translations", ignore = true)
  public abstract MessageDTO asDTO(Message domain, @Context Map<String, Object> context);

  @AfterMapping
  protected void afterConvert(
      MessageDTO dto, @MappingTarget Message domain, @Context Map<String, Object> context) {
    if (context != null && context.get("iso3Language") != null && domain.getId() != null) {
      MessageTrl messageTrl =
          messageService.getMessageTrlByMessageIdAndLanguage(
              domain.getId(), (String) context.get("iso3Language"));
      if (messageTrl == null) {
        messageTrl = new MessageTrl();
      }
      messageTrl.setValue(dto.getName());
      domain.getTranslations().put((String) context.get("iso3Language"), messageTrl);
    }
  }

  @AfterMapping
  protected void afterConvert(
      Message domain, @MappingTarget MessageDTO dto, @Context Map<String, Object> context) {
    if (context != null && context.get("iso3Language") != null) {
      MessageTrl trainingTrl =
          messageService.getMessageTrlByMessageIdAndLanguage(
              domain.getId(), (String) context.get("iso3Language"));
      dto.setName(trainingTrl.getValue());
    }
  }
}