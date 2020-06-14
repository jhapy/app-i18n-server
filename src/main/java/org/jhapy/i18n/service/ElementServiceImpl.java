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
import org.apache.commons.lang3.StringUtils;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.repository.ElementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Service
@Transactional(readOnly = true)
public class ElementServiceImpl implements ElementService {

  private final ElementRepository elementRepository;
  private final ElementTrlService elementTrlService;

  public ElementServiceImpl(ElementRepository elementRepository,
      ElementTrlService elementTrlService) {
    this.elementRepository = elementRepository;
    this.elementTrlService = elementTrlService;
  }

  @Override
  public Page<Element> findByNameLike(String name, Pageable pageable) {
    return elementRepository.findByNameLike(name, pageable);
  }

  @Override
  public long countByNameLike(String name) {
    return elementRepository.countByNameLike(name);
  }

  @Override
  public Page<Element> findAnyMatching(String filter, Pageable pageable) {
    if (StringUtils.isBlank(filter)) {
      return elementRepository.findAll(pageable);
    } else {
      return elementRepository.findAnyMatching(filter, pageable);
    }
  }

  @Override
  public long countAnyMatching(String filter) {
    if (StringUtils.isBlank(filter)) {
      return elementRepository.count();
    } else {
      return elementRepository.countAnyMatching(filter);
    }
  }

  @Override
  @Transactional
  public Element save(Element entity) {
    List<ElementTrl> translations = entity.getTranslations();
    entity = elementRepository.save(entity);
    for (ElementTrl elementTrl : translations) {
      elementTrl.setElement(entity);
    }
    if (translations.size() > 0) {
      entity.setTranslations(elementTrlService.saveAll(translations));
    }

    return entity;
  }

  @Override
  @Transactional
  public void delete(Element entity) {
    List<ElementTrl> elementTrls = elementTrlService.findByElement(entity.getId());
    if (elementTrls.size() > 0) {
      elementTrlService.deleteAll(elementTrls);
    }

    elementRepository.delete(entity);
  }

  @Override
  public JpaRepository<Element, Long> getRepository() {
    return elementRepository;
  }
}
