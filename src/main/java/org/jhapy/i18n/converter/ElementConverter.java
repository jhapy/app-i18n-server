package org.jhapy.i18n.converter;

import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.service.ElementService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Mapper(
    config = BaseRelationalDbConverterConfig.class,
    componentModel = "spring",
    uses = {RelationalDbReferenceMapper.class, ElementTrlConverter.class})
public abstract class ElementConverter extends GenericMapper<Element, ElementDTO> {
  @Autowired private ElementService elementService;

  @Mapping(target = "translations", ignore = true)
  public abstract Element asEntity(ElementDTO dto, @Context Map<String, Object> context);

  @Mapping(target = "translations", ignore = true)
  public abstract ElementDTO asDTO(Element domain, @Context Map<String, Object> context);

  @AfterMapping
  protected void afterConvert(
      ElementDTO dto, @MappingTarget Element domain, @Context Map<String, Object> context) {
    if (context != null && context.get("iso3Language") != null && domain.getId() != null) {
      ElementTrl elementTrl =
          elementService.getElementTrlByElementIdAndLanguage(
              domain.getId(), (String) context.get("iso3Language"));
      if (elementTrl == null) {
        elementTrl = new ElementTrl();
      }
      elementTrl.setValue(dto.getName());
      domain.getTranslations().put((String) context.get("iso3Language"), elementTrl);
    }
  }

  @AfterMapping
  protected void afterConvert(
      Element domain, @MappingTarget ElementDTO dto, @Context Map<String, Object> context) {
    if (context != null && context.get("iso3Language") != null) {
      ElementTrl trainingTrl =
          elementService.getElementTrlByElementIdAndLanguage(
              domain.getId(), (String) context.get("iso3Language"));
      dto.setName(trainingTrl.getValue());
    }
  }
}