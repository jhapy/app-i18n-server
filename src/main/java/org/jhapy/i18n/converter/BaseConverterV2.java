package org.jhapy.i18n.converter;

import org.jhapy.commons.converter.CommonsConverterV2;
import org.jhapy.dto.domain.BaseEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 18/05/2021
 */
@Mapper(componentModel = "spring")
public abstract class BaseConverterV2 extends CommonsConverterV2 {

  @AfterMapping
  public void afterConvert(BaseEntity dto, @MappingTarget org.jhapy.i18n.domain.BaseEntity domain) {
    if (dto.getIsNew()) {
      domain.setId(null);
    }
  }
}