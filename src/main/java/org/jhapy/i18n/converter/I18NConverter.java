package org.jhapy.i18n.converter;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.utils.Page;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@Component
public class I18NConverter implements HasLogger {

  private final OrikaBeanMapper orikaBeanMapper;

  public I18NConverter(OrikaBeanMapper orikaBeanMapper) {
    this.orikaBeanMapper = orikaBeanMapper;
  }

  @Bean
  public void i18NConverters() {
    orikaBeanMapper.addMapper(Action.class, org.jhapy.dto.domain.i18n.Action.class);
    orikaBeanMapper
        .getClassMapBuilder(ActionTrl.class, org.jhapy.dto.domain.i18n.ActionTrl.class)
        .byDefault().customize(
        new CustomMapper<ActionTrl, org.jhapy.dto.domain.i18n.ActionTrl>() {
          @Override
          public void mapAtoB(ActionTrl a, org.jhapy.dto.domain.i18n.ActionTrl b,
              MappingContext context) {
            b.setName(a.getAction().getName());
          }
        }).register();

    orikaBeanMapper.addMapper(Element.class, org.jhapy.dto.domain.i18n.Element.class);
    orikaBeanMapper
        .getClassMapBuilder(ElementTrl.class, org.jhapy.dto.domain.i18n.ElementTrl.class)
        .byDefault().customize(
        new CustomMapper<ElementTrl, org.jhapy.dto.domain.i18n.ElementTrl>() {
          @Override
          public void mapAtoB(ElementTrl a, org.jhapy.dto.domain.i18n.ElementTrl b,
              MappingContext context) {
            b.setName(a.getElement().getName());
          }
        }).register();

    orikaBeanMapper.addMapper(Message.class, org.jhapy.dto.domain.i18n.Message.class);
    orikaBeanMapper
        .getClassMapBuilder(MessageTrl.class, org.jhapy.dto.domain.i18n.MessageTrl.class)
        .byDefault().customize(
        new CustomMapper<MessageTrl, org.jhapy.dto.domain.i18n.MessageTrl>() {
          @Override
          public void mapAtoB(MessageTrl a, org.jhapy.dto.domain.i18n.MessageTrl b,
              MappingContext context) {
            b.setName(a.getMessage().getName());
          }
        }).register();

    orikaBeanMapper.addMapper(PageImpl.class, Page.class);
    orikaBeanMapper.addMapper(Page.class, PageImpl.class);
  }
}