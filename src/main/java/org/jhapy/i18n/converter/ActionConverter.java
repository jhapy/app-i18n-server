package org.jhapy.i18n.converter;

import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.service.ActionService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = {RelationalDbReferenceMapper.class, ActionTrlConverter.class})
public abstract class ActionConverter extends GenericMapper<Action, ActionDTO> {
  @Autowired private ActionService actionService;

  @Mapping(target = "translations", ignore = true)
  public abstract Action asEntity(ActionDTO dto, @Context Map<String, Object> context);

  @Mapping(target = "translations", ignore = true)
  public abstract ActionDTO asDTO(Action domain, @Context Map<String, Object> context);

  @AfterMapping
  protected void afterConvert(
      ActionDTO dto, @MappingTarget Action domain, @Context Map<String, Object> context) {
    if (context != null && context.get("iso3Language") != null && domain.getId() != null) {
      ActionTrl actionTrl =
          actionService.getActionTrlByActionIdAndLanguage(
              domain.getId(), (String) context.get("iso3Language"));
      if (actionTrl == null) {
        actionTrl = new ActionTrl();
      }
      actionTrl.setValue(dto.getName());
      domain.getTranslations().put((String) context.get("iso3Language"), actionTrl);
    }
  }

  @AfterMapping
  protected void afterConvert(
      Action domain, @MappingTarget ActionDTO dto, @Context Map<String, Object> context) {
    if (context != null && context.get("iso3Language") != null) {
      ActionTrl trainingTrl =
          actionService.getActionTrlByActionIdAndLanguage(
              domain.getId(), (String) context.get("iso3Language"));
      dto.setName(trainingTrl.getValue());
    }
  }
}