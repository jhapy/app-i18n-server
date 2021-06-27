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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.messageQueue.I18NActionUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.converter.I18NConverterV2;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.repository.ActionRepository;
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
public class ActionServiceImpl implements ActionService {

  private final ActionRepository actionRepository;
  private final ActionTrlService actionTrlService;
  private final VersionRepository versionRepository;
  private final I18NQueue i18NQueue;
  private final EntityManager entityManager;
  private final I18NConverterV2 i18NConverterV2;

  public ActionServiceImpl(ActionRepository actionRepository,
      ActionTrlService actionTrlService,
      VersionRepository versionRepository,
      I18NConverterV2 i18NConverterV2,
      I18NQueue i18NQueue, EntityManager entityManager) {
    this.actionRepository = actionRepository;
    this.actionTrlService = actionTrlService;
    this.versionRepository = versionRepository;
    this.i18NQueue = i18NQueue;
    this.entityManager = entityManager;
    this.i18NConverterV2 = i18NConverterV2;
  }

  @Override
  @Transactional
  public Action save(Action entity) {
    List<ActionTrl> translations = entity.getTranslations();
    entity = actionRepository.save(entity);
    for (ActionTrl actionTrl : translations) {
      actionTrl.setAction(entity);
    }
    if (translations.size() > 0) {
      entity.setTranslations(actionTrlService.saveAll(translations));
    }

    return entity;
  }

  @Override
  @Transactional
  public void postUpdate(Action action) {
    if (actionTrlService.hasBootstrapped()) {
      I18NActionUpdate actionUpdate = new I18NActionUpdate();
      actionUpdate.setAction(i18NConverterV2.convertToDto(action));
      actionUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
      i18NQueue.sendActionUpdate(actionUpdate);
    }
  }

  @Override
  @Transactional
  public void postPersist(Action action) {
    if (actionTrlService.hasBootstrapped()) {
      I18NActionUpdate actionUpdate = new I18NActionUpdate();
      actionUpdate.setAction(i18NConverterV2.convertToDto(action));
      actionUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
      i18NQueue.sendActionUpdate(actionUpdate);
    }
  }

  @Override
  @Transactional
  public void postRemove(Action action) {
    if (actionTrlService.hasBootstrapped()) {
      I18NActionUpdate actionUpdate = new I18NActionUpdate();
      actionUpdate.setAction(i18NConverterV2.convertToDto(action));
      actionUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
      i18NQueue.sendActionUpdate(actionUpdate);
    }
  }

  @Override
  @Transactional
  public void delete(Action entity) {
    List<ActionTrl> actionTrls = actionTrlService.findByAction(entity.getId());
    if (actionTrls.size() > 0) {
      actionTrlService.deleteAll(actionTrls);
    }

    actionRepository.delete(entity);

    versionRepository.incActionRecords();
  }

  @Override
  public CriteriaQuery buildSearchQuery(CriteriaQuery query, Root<Action> entity,
      CriteriaBuilder cb, String currentUserId, String filter, Boolean showInactive,
      Object... otherCriteria) {
    List<Predicate> orPredicates = new ArrayList<>();
    List<Predicate> andPredicated = new ArrayList<>();

    if (StringUtils.isNotBlank(filter)) {
      Join<Action, ActionTrl> join = entity.join("translations", JoinType.LEFT);
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

  @Override
  public JpaRepository<Action, Long> getRepository() {
    return actionRepository;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public Class<Action> getEntityClass() {
    return Action.class;
  }
}
