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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jhapy.dto.messageQueue.I18NMessageTrlUpdate;
import org.jhapy.dto.messageQueue.I18NMessageUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.converter.MessageConverter;
import org.jhapy.i18n.converter.MessageTrlConverter;
import org.jhapy.i18n.domain.*;
import org.jhapy.i18n.repository.BaseRepository;
import org.jhapy.i18n.repository.MessageRepository;
import org.jhapy.i18n.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Service
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final VersionRepository versionRepository;
  private final I18NQueue i18NQueue;
  private final EntityManager entityManager;
  private final MessageConverter messageConverter;
  private final MessageTrlConverter messageTrlConverter;
  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private Boolean isBootstrapEnabled;

  public MessageServiceImpl(
      MessageRepository messageRepository,
      VersionRepository versionRepository,
      I18NQueue i18NQueue,
      EntityManager entityManager,
      MessageConverter messageConverter,
      @Lazy MessageTrlConverter messageTrlConverter) {
    this.messageRepository = messageRepository;
    this.versionRepository = versionRepository;
    this.i18NQueue = i18NQueue;
    this.entityManager = entityManager;
    this.messageConverter = messageConverter;
    this.messageTrlConverter = messageTrlConverter;
  }

  @Override
  @Transactional
  public MessageTrl getMessageTrlByMessageIdAndLanguage(Long messageId, String iso3Language) {
    var message = messageRepository.getById(messageId);
    if (message.getTranslations().containsKey(iso3Language))
      return message.getTranslations().get(iso3Language);
    else {
      var optMessageTrl =
          message.getTranslations().values().stream()
              .filter(
                  messageTrl -> (messageTrl.getIsDefault() != null && messageTrl.getIsDefault()))
              .findFirst();
      var messageTrl = new MessageTrl();
      if (optMessageTrl.isPresent()) {
        var defaultMessageTrl = optMessageTrl.get();
        messageTrl.setValue(defaultMessageTrl.getValue());
      } else {
        messageTrl.setValue(message.getName());
      }
      messageTrl.setIsDefault(false);
      messageTrl.setIsTranslated(false);
      message = messageRepository.getById(messageId);
      message.getTranslations().put(iso3Language, messageTrl);
      message = messageRepository.save(message);
      return message.getTranslations().get(iso3Language);
    }
  }

  @Override
  public List<MessageTrl> getMessageTrlByIso3Language(String iso3Language) {
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    return messageRepository.findByIso3(iso3Language).stream()
        .map(
            messageTrlProjection -> {
              MessageTrl messageTrl = new MessageTrl();
              messageTrl.setName(messageTrlProjection.getName());
              messageTrl.setValue(messageTrlProjection.getValue());
              messageTrl.setIso3Language(iso3Language);
              messageTrl.setIsDefault(messageTrlProjection.getIsDefault());
              messageTrl.setIsTranslated(messageTrlProjection.getIsTranslated());
              messageTrl.setRelatedEntityId(messageTrlProjection.getRelatedEntityId());
              return messageTrl;
            })
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public MessageTrl getByMessageTrlNameAndLanguage(String name, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getByNameAndIso3Language", name, iso3Language);

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Message> _message = messageRepository.getByName(name);
    Message message;
    if (_message.isEmpty()) {
      logger().warn(loggerPrefix + "Message '" + name + "' not found, create a new one");
      message = new Message();
      message.setName(name);
      message.setIsTranslated(false);
      message = messageRepository.save(message);
    } else {
      message = _message.get();
    }
    if (message.getTranslations() != null && message.getTranslations().containsKey(iso3Language)) {
      return message.getTranslations().get(iso3Language);
    } else {
      logger()
          .warn(
              loggerPrefix
                  + "Message '"
                  + name
                  + "', '"
                  + iso3Language
                  + "' language translation not found, create a new one");
      if (message.getTranslations() == null) message.setTranslations(new HashMap<>());
      var optMessageTrl =
          message.getTranslations().values().stream()
              .filter(EntityTranslationV2::getIsDefault)
              .findFirst();
      var messageTrl = new MessageTrl();
      if (optMessageTrl.isPresent()) {
        var defaultMessageTrl = optMessageTrl.get();
        messageTrl.setValue(defaultMessageTrl.getValue());
      } else {
        messageTrl.setValue(name);
      }
      messageTrl.setIsDefault(false);
      messageTrl.setIsTranslated(false);
      message.getTranslations().put(iso3Language, messageTrl);
      message = messageRepository.save(message);
      return message.getTranslations().get(iso3Language);
    }
  }

  @Override
  public List<MessageTrl> getMessageTrls(Long messageId) {
    var message = messageRepository.getById(messageId);

    return message.getTranslations().keySet().stream()
        .map(
            key -> {
              var messageTrl = message.getTranslations().get(key);
              messageTrl.setRelatedEntityId(messageId);
              messageTrl.setIso3Language(key);
              return messageTrl;
            })
        .toList();
  }

  @Override
  @Transactional
  public void postUpdate(Message message) {
    var messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(messageConverter.asDTO(message, null));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public void postPersist(Message message) {
    var messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(messageConverter.asDTO(message, null));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Override
  @Transactional
  public void postRemove(Message message) {
    var messageUpdate = new I18NMessageUpdate();
    messageUpdate.setMessage(messageConverter.asDTO(message, null));
    messageUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
    i18NQueue.sendMessageUpdate(messageUpdate);
  }

  @Transactional
  @Override
  public void postUpdate(MessageTrl messageTrl) {
    var isAllTranslated = true;
    var message = messageRepository.getById(messageTrl.getRelatedEntityId());
    Map<String, MessageTrl> trls = message.getTranslations();
    for (MessageTrl trl : trls.values()) {
      if (!trl.getIsTranslated()) {
        isAllTranslated = false;
        break;
      }
    }
    if (isAllTranslated && !message.getIsTranslated()) {
      message.setIsTranslated(true);
      messageRepository.save(message);
    } else if (!isAllTranslated && message.getIsTranslated()) {
      message.setIsTranslated(false);
      messageRepository.save(message);
    }
    var messageTrlUpdate = new I18NMessageTrlUpdate();
    messageTrlUpdate.setMessageTrl(messageTrlConverter.asDTO(messageTrl, null));
    messageTrlUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendMessageTrlUpdate(messageTrlUpdate);
  }

  @Override
  @Transactional
  public void postPersist(MessageTrl messageTrl) {
    var messageTrlUpdate = new I18NMessageTrlUpdate();
    messageTrlUpdate.setMessageTrl(messageTrlConverter.asDTO(messageTrl, null));
    messageTrlUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendMessageTrlUpdate(messageTrlUpdate);
  }

  @Override
  @Transactional
  public void postRemove(MessageTrl messageTrl) {
    var messageTrlUpdate = new I18NMessageTrlUpdate();
    messageTrlUpdate.setMessageTrl(messageTrlConverter.asDTO(messageTrl, null));
    messageTrlUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
    i18NQueue.sendMessageTrlUpdate(messageTrlUpdate);
  }

  @Override
  @Transactional
  public void delete(Message entity) {
    MessageService.super.delete(entity);

    versionRepository.incMessageRecords();
  }

  @Override
  public Specification<Message> buildSearchQuery(
      String filter, Boolean showInactive, Object... otherCriteria) {
    if (StringUtils.isNotBlank(filter)) {
      var criterias = new ArrayList<>();
      criterias.add(
          new RelationalDbSearchCriteria("name", filter, RelationalDbSearchOperation.MATCH));
      criterias.add(
          new RelationalDbSearchCriteria("category", filter, RelationalDbSearchOperation.MATCH));
      criterias.add(
          new RelationalDbSearchCriteria(
              "translations.value", filter, RelationalDbSearchOperation.MATCH));

      var result = new RelationalDbSpecification<Message>();
      result.or(criterias.toArray(new RelationalDbSearchCriteria[0]));
      return result;
    } else {
      return null;
    }
  }

  @Transactional
  @Override
  public void reset() {
    List<Message> allMessages = messageRepository.findAll();
    allMessages.forEach(
        message ->
            message
                .getTranslations()
                .values()
                .forEach(messageTrl -> messageTrl.setIsTranslated(false)));
    messageRepository.saveAll(allMessages);
    versionRepository.incActionRecords();
  }

  @Override
  public boolean hasBootstrapped() {
    return hasBootstrapped;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  public void postLoad() {
    bootstrapMessages();
  }

  @Transactional
  public synchronized void bootstrapMessages() {
    if (hasBootstrapped) {
      return;
    }

    if (!isBootstrapEnabled) {
      hasBootstrapped = true;
      return;
    }

    var loggerPrefix = getLoggerPrefix("bootstrapMessages");
    try {
      importExcelFile(Files.readAllBytes(Path.of(bootstrapFile)));
    } catch (IOException e) {
      error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
  }

  @Transactional
  public String importExcelFile(byte[] content) {
    var loggerPrefix = getLoggerPrefix("importExcelFile");
    logger().info(loggerPrefix + "Clean data");
    messageRepository.deleteAll();
    try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
      var sheet = workbook.getSheet("Messages");
      if (sheet == null) {
        sheet = workbook.getSheet("messages");
      }

      info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

      Iterator<Row> rowIterator = sheet.rowIterator();
      var rowIndex = 0;

      while (rowIterator.hasNext()) {
        var row = rowIterator.next();
        rowIndex++;
        if (rowIndex == 1) {
          continue;
        }

        if (rowIndex % 10 == 0) {
          info(loggerPrefix, "Handle row {0}", rowIndex);
        }

        var colIdx = 0;
        var categoryCell = row.getCell(colIdx++);
        var name0Cell = row.getCell(colIdx++);
        var name1Cell = row.getCell(colIdx++);
        var name2Cell = row.getCell(colIdx++);
        var name3Cell = row.getCell(colIdx++);
        var langCell = row.getCell(colIdx++);
        var valueCell = row.getCell(colIdx);

        if (langCell == null) {
          error(loggerPrefix, "Empty value for language, skip");
          continue;
        }

        if (name0Cell == null) {
          error(loggerPrefix, "Empty value for name, skip");
          continue;
        }

        String category = categoryCell == null ? null : categoryCell.getStringCellValue();

        var name = name0Cell.getStringCellValue();
        if (name1Cell != null && StringUtils.isNotBlank(name1Cell.getStringCellValue())) {
          name += "." + name1Cell.getStringCellValue();
        }
        if (name2Cell != null && StringUtils.isNotBlank(name2Cell.getStringCellValue())) {
          name += "." + name2Cell.getStringCellValue();
        }
        if (name3Cell != null && StringUtils.isNotBlank(name3Cell.getStringCellValue())) {
          name += "." + name3Cell.getStringCellValue();
        }

        var language = langCell.getStringCellValue();

        Optional<Message> optMessage = messageRepository.getByName(name);
        Message message;
        if (optMessage.isEmpty()) {
          message = new Message();
          message.setName(name);
          message.setCategory(category);
          message.setIsTranslated(true);

          debug(loggerPrefix, "Create : {0}", message);

        } else {
          message = optMessage.get();
          message.setCategory(category);
          message.setIsTranslated(true);

          debug(loggerPrefix, "Update : {0}", message);
        }
        message = messageRepository.save(message);

        MessageTrl messageTrl = message.getTranslations().get(language);
        if (messageTrl == null) {
          messageTrl = new MessageTrl();
          messageTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          messageTrl.setIso3Language(language);
          messageTrl.setIsTranslated(true);

          debug(loggerPrefix, "Create Trl : {0}", messageTrl);
          message.getTranslations().put(language, messageTrl);
          messageRepository.save(message);
        } else {
          if (!messageTrl.getIsTranslated()) {
            messageTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            messageTrl.setIso3Language(language);
            messageTrl.setIsTranslated(true);

            debug(loggerPrefix, "Update Trl : {0}", messageTrl);

            message.getTranslations().put(language, messageTrl);
            messageRepository.save(message);
          }
        }
      }
    } catch (IOException e) {
      error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
      return e.getMessage();
    }

    info(loggerPrefix, "Done");

    hasBootstrapped = true;

    return null;
  }

  @Override
  public BaseRepository<Message> getRepository() {
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
}