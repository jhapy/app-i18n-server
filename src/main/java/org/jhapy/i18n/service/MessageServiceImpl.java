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
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.messageQueue.I18NElementUpdate;
import org.jhapy.dto.messageQueue.I18NMessageUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.MessageRepository;
import org.jhapy.i18n.repository.VersionRepository;
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
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final MessageTrlService messageTrlService;
  private final VersionRepository versionRepository;
  private final OrikaBeanMapper mapperFacade;
  private final I18NQueue i18NQueue;

  public MessageServiceImpl(MessageRepository messageRepository,
      MessageTrlService messageTrlService,
      VersionRepository versionRepository, OrikaBeanMapper mapperFacade,
      I18NQueue i18NQueue) {
    this.messageRepository = messageRepository;
    this.messageTrlService = messageTrlService;
    this.versionRepository = versionRepository;
    this.mapperFacade = mapperFacade;
    this.i18NQueue = i18NQueue;
  }

  @Override
  public Page<Message> findByNameLike(String name, Pageable pageable) {
    return messageRepository.findByNameLike(name, pageable);
  }

  @Override
  public long countByNameLike(String name) {
    return messageRepository.countByNameLike(name);
  }

  @Override
  public Page<Message> findAnyMatching(String filter, Pageable pageable) {
    if (StringUtils.isBlank(filter)) {
      return messageRepository.findAll(pageable);
    } else {
      return messageRepository.findAnyMatching(filter, pageable);
    }
  }

  @Override
  public long countAnyMatching(String filter) {
    if (StringUtils.isBlank(filter)) {
      return messageRepository.count();
    } else {
      return messageRepository.countAnyMatching(filter);
    }
  }

  @Override
  @Transactional
  public void postUpdate(Message message) {
    I18NMessageUpdate messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(mapperFacade.map( message, org.jhapy.dto.domain.i18n.Message.class));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public void postPersist(Message message) {
    I18NMessageUpdate messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(mapperFacade.map( message, org.jhapy.dto.domain.i18n.Message.class));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public void postRemove(Message message) {
    I18NMessageUpdate messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(mapperFacade.map( message, org.jhapy.dto.domain.i18n.Message.class));
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
    if (translations.size() > 0) {
      entity.setTranslations(messageTrlService.saveAll(translations));
    }
    versionRepository.incMessageRecords();
    return entity;
  }

  @Override
  @Transactional
  public void delete(Message entity) {
    List<MessageTrl> messageTrls = messageTrlService.findByMessage(entity.getId());
    if (messageTrls.size() > 0) {
      messageTrlService.deleteAll(messageTrls);
    }

    messageRepository.delete(entity);

    versionRepository.incMessageRecords();
  }

  @Override
  public JpaRepository<Message, Long> getRepository() {
    return messageRepository;
  }
}
