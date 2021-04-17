/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.i18n.converter;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.utils.Page;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

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

    orikaBeanMapper.getClassMapBuilder(Action.class, org.jhapy.dto.domain.i18n.Action.class).exclude("translations").byDefault().register();
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

    orikaBeanMapper.getClassMapBuilder(Element.class, org.jhapy.dto.domain.i18n.Element.class).exclude("translations").byDefault().register();
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

    orikaBeanMapper.getClassMapBuilder(Message.class, org.jhapy.dto.domain.i18n.Message.class).exclude("translations").byDefault().register();
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