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

package org.jhapy.i18n.service;

import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;

import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
public interface ElementService extends CrudRelationalService<Element> {

  void postUpdate(Element element);

  void postUpdate(ElementTrl elementTrl);

  void postPersist(Element element);

  void postPersist(ElementTrl elementTrl);

  void postRemove(Element element);

  void postRemove(ElementTrl elementTrl);

  ElementTrl getElementTrlByElementIdAndLanguage(Long elementId, String iso3Language);

  ElementTrl getByElementTrlNameAndLanguage(String name, String iso3Language);

  List<ElementTrl> getElementTrlByIso3Language(String iso3Language);

  List<ElementTrl> getElementTrls(Long elementId);

  boolean hasBootstrapped();

  String importExcelFile(byte[] content);

  void reset();
}