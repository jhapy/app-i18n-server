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
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.repository.ActionRepository;
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
public class ActionServiceImpl implements ActionService {

  private final ActionRepository actionRepository;
  private final ActionTrlService actionTrlService;

  public ActionServiceImpl(ActionRepository actionRepository,
      ActionTrlService actionTrlService) {
    this.actionRepository = actionRepository;
    this.actionTrlService = actionTrlService;
  }

  @Override
  public Page<Action> findByNameLike(String name, Pageable pageable) {
    return actionRepository.findByNameLike(name, pageable);
  }

  @Override
  public long countByNameLike(String name) {
    return actionRepository.countByNameLike(name);
  }

  @Override
  public Page<Action> findAnyMatching(String filter, Pageable pageable) {
    if (StringUtils.isBlank(filter)) {
      return actionRepository.findAll(pageable);
    } else {
      return actionRepository.findAnyMatching(filter, pageable);
    }
  }

  @Override
  public long countAnyMatching(String filter) {
    if (StringUtils.isBlank(filter)) {
      return actionRepository.count();
    } else {
      return actionRepository.countAnyMatching(filter);
    }
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
  public void delete(Action entity) {
    List<ActionTrl> actionTrls = actionTrlService.findByAction(entity.getId());
    if (actionTrls.size() > 0) {
      actionTrlService.deleteAll(actionTrls);
    }

    actionRepository.delete(entity);
  }

  @Override
  public JpaRepository<Action, Long> getRepository() {
    return actionRepository;
  }
}
