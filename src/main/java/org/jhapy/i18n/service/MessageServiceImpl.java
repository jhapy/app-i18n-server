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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.messageQueue.I18NMessageUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.MessageRepository;
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
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final MessageTrlService messageTrlService;
  private final VersionRepository versionRepository;
  private final OrikaBeanMapper mapperFacade;
  private final I18NQueue i18NQueue;
  private final EntityManager entityManager;

  public MessageServiceImpl(MessageRepository messageRepository,
      MessageTrlService messageTrlService,
      VersionRepository versionRepository, OrikaBeanMapper mapperFacade,
      I18NQueue i18NQueue, EntityManager entityManager) {
    this.messageRepository = messageRepository;
    this.messageTrlService = messageTrlService;
    this.versionRepository = versionRepository;
    this.mapperFacade = mapperFacade;
    this.i18NQueue = i18NQueue;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public void postUpdate(Message message) {
    var messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(mapperFacade.map(message, org.jhapy.dto.domain.i18n.Message.class));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public void postPersist(Message message) {
    var messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(mapperFacade.map(message, org.jhapy.dto.domain.i18n.Message.class));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public void postRemove(Message message) {
    var messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(mapperFacade.map(message, org.jhapy.dto.domain.i18n.Message.class));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public Message save(Message entity) {
    List<MessageTrl> translations = entity.getTranslations();
    entity = messageRepository.save(entity);
    for (MessageTrl messageTrl : translations) {
      messageTrl.setMessage(entity);
    }
    if (!translations.isEmpty()) {
      entity.setTranslations(messageTrlService.saveAll(translations));
    }
    versionRepository.incMessageRecords();
    return entity;
  }

  @Override
  @Transactional
  public void delete(Message entity) {
    List<MessageTrl> messageTrls = messageTrlService.findByMessage(entity.getId());
    if (!messageTrls.isEmpty()) {
      messageTrlService.deleteAll(messageTrls);
    }

    messageRepository.delete(entity);

    versionRepository.incMessageRecords();
  }

  @Override
  public JpaRepository<Message, Long> getRepository() {
    return messageRepository;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public Class<Message> getEntityClass() {
    return Message.class;
  }

  @Override
  public CriteriaQuery buildSearchQuery(CriteriaQuery query, Root<Message> entity,
      CriteriaBuilder cb, String currentUserId, String filter, Boolean showInactive,
      Object... otherCriteria) {
    List<Predicate> orPredicates = new ArrayList<>();
    List<Predicate> andPredicated = new ArrayList<>();

    if (StringUtils.isNotBlank(filter)) {
      Join<Message, MessageTrl> join = entity.join("translations");
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
