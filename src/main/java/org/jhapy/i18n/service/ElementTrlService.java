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

import java.util.List;
import org.jhapy.i18n.domain.ElementTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
public interface ElementTrlService extends CrudService<ElementTrl> {

  List<ElementTrl> findByElement(Long elementId);

  long countByElement(Long elementId);

  ElementTrl getByNameAndIso3Language(String name, String iso3Language);

  List<ElementTrl> getByIso3Language(String iso3Language);

  List<ElementTrl> saveAll(List<ElementTrl> translations);

  void deleteAll(List<ElementTrl> elementTrls);

  void postUpdate(ElementTrl elementTrl);

  String importExcelFile(byte[] content);

  void reset();
}
