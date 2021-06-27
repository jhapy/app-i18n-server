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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.messageQueue.I18NElementUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.converter.I18NConverterV2;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.repository.ElementRepository;
import org.jhapy.i18n.repository.VersionRepository;
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
  private final VersionRepository versionRepository;
  private final I18NQueue i18NQueue;
  private final EntityManager entityManager;
  private final I18NConverterV2 i18NConverterV2;

  public ElementServiceImpl(ElementRepository elementRepository,
      ElementTrlService elementTrlService,
      VersionRepository versionRepository,
      I18NQueue i18NQueue, EntityManager entityManager,
      I18NConverterV2 i18NConverterV2) {
    this.elementRepository = elementRepository;
    this.elementTrlService = elementTrlService;
    this.versionRepository = versionRepository;
    this.i18NConverterV2 = i18NConverterV2;
    this.i18NQueue = i18NQueue;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public void postUpdate(Element element) {
    if (elementTrlService.hasBootstrapped()) {
      I18NElementUpdate elementUpdate = new I18NElementUpdate();
      elementUpdate.setElement(i18NConverterV2.convertToDto(element));
      elementUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
      i18NQueue.sendElementUpdate(elementUpdate);
    }
  }

  @Override
  @Transactional
  public void postPersist(Element element) {
    if (elementTrlService.hasBootstrapped()) {
      I18NElementUpdate elementUpdate = new I18NElementUpdate();
      elementUpdate.setElement(i18NConverterV2.convertToDto(element));
      elementUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
      i18NQueue.sendElementUpdate(elementUpdate);
    }
  }

  @Override
  @Transactional
  public void postRemove(Element element) {
    if (elementTrlService.hasBootstrapped()) {
      I18NElementUpdate elementUpdate = new I18NElementUpdate();
      elementUpdate.setElement(i18NConverterV2.convertToDto(element));
      elementUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
      i18NQueue.sendElementUpdate(elementUpdate);
    }
  }

  @Override
  @Transactional
  public Element save(Element entity) {
    List<ElementTrl> translations = entity.getTranslations();
    if ( entity.getId() == null ) {
      Optional<Element> optExisting = elementRepository.getByName(entity.getName());
      if ( optExisting.isPresent() )
        entity.setId(optExisting.get().getId());
    }
    entity = elementRepository.save(entity);
    for (ElementTrl elementTrl : translations) {
      elementTrl.setElement(entity);
    }
    if (translations.size() > 0) {
      entity.setTranslations(elementTrlService.saveAll(translations));
    }

    versionRepository.incElementRecords();

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

    versionRepository.incElementRecords();
  }

  @Override
  public JpaRepository<Element, Long> getRepository() {
    return elementRepository;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public Class<Element> getEntityClass() {
    return Element.class;
  }

  @Override
  public CriteriaQuery buildSearchQuery(CriteriaQuery query, Root entity,
      CriteriaBuilder cb, String currentUserId, String filter, Boolean showInactive,
      Object... otherCriteria) {
    List<Predicate> orPredicates = new ArrayList<>();
    List<Predicate> andPredicated = new ArrayList<>();

    if (StringUtils.isNotBlank(filter)) {
      Join<Element, ElementTrl> join = entity.join("translations", JoinType.LEFT);
      String pattern = "%" + filter.toLowerCase() + "%";
      orPredicates.add(cb.like(cb.lower(entity.get("name")), pattern));
      orPredicates.add(cb.like(cb.lower(entity.get("category")), pattern));
      orPredicates.add(cb.like(cb.lower(join.get("value")), pattern));
    }

    if (showInactive == null || !showInactive) {
      andPredicated.add(cb.equal(entity.get("isActive"), Boolean.TRUE));
    }

    if (!orPredicates.isEmpty()) {
      andPredicated.add(cb.or(orPredicates.toArray(new Predicate[0])));
    }

    if (!andPredicated.isEmpty()) {
      query.where(cb.and(andPredicated.toArray(new Predicate[0])));
    }
    return query;
  }
}
