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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.messageQueue.I18NMessageTrlUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.MessageRepository;
import org.jhapy.i18n.repository.MessageTrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
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
public class MessageTrlServiceImpl implements MessageTrlService, HasLogger {

  private final MessageRepository messageRepository;
  private final MessageTrlRepository messageTrlRepository;
  private final OrikaBeanMapper mapperFacade;
  private final I18NQueue i18NQueue;

  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private boolean isBootstrapEnabled;

  public MessageTrlServiceImpl(MessageRepository messageRepository,
      MessageTrlRepository messageTrlRepository,
      OrikaBeanMapper mapperFacade,
      I18NQueue i18NQueue) {
    this.messageRepository = messageRepository;
    this.messageTrlRepository = messageTrlRepository;
    this.mapperFacade = mapperFacade;
    this.i18NQueue = i18NQueue;
  }

  @Transactional
  @Override
  public void reset() {
    List<MessageTrl> allMessages = messageTrlRepository.findAll();
    allMessages.forEach(actionTrl -> actionTrl.setIsTranslated(false));
    messageTrlRepository.saveAll(allMessages);
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

  @Override
  public List<MessageTrl> findByMessage(Long messageId) {
    Optional<Message> message = messageRepository.findById(messageId);
    if (message.isPresent()) {
      return messageTrlRepository.findByMessage(message.get());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public long countByMessage(Long messageId) {
    return messageRepository.findById(messageId).map(messageTrlRepository::countByMessage)
        .orElse(0L);
  }

  @Transactional
  @Override
  public MessageTrl getByNameAndIso3Language(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getByNameAndIso3Language");

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Message> optMessage = messageRepository.getByName(name);
    Message message;
    if (optMessage.isEmpty()) {
      warn(loggerPrefix, "Message '{0}' not found, create a new one", name);
      message = new Message();
      message.setName(name);
      message.setIsTranslated(false);
      message = messageRepository.save(message);
    } else {
      message = optMessage.get();
    }
    Optional<MessageTrl> optMessageTrl = messageTrlRepository
        .getByMessageAndIso3Language(message, iso3Language);
    if (optMessageTrl.isPresent()) {
      return optMessageTrl.get();
    } else {
      warn(loggerPrefix, "Message ''{0}', '{1}' language translation not found, create a new one",
          name, iso3Language);
      var messageTrl = new MessageTrl();
      messageTrl.setIso3Language(iso3Language);
      messageTrl.setMessage(message);

      Optional<MessageTrl> optDefaultMessageTrl = messageTrlRepository
          .getByMessageAndIsDefault(message, true);
      if (optDefaultMessageTrl.isPresent()) {
        messageTrl.setValue(optDefaultMessageTrl.get().getValue());
      } else {
        messageTrl.setValue(name);
      }
      messageTrl.setIsTranslated(false);

      messageTrl = messageTrlRepository.save(messageTrl);
      return messageTrl;
    }
  }

  @Override
  public List<MessageTrl> getByIso3Language(String iso3Language) {
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    return messageTrlRepository.findByIso3Language(iso3Language);
  }

  @Override
  public List<MessageTrl> saveAll(List<MessageTrl> translations) {
    return messageTrlRepository.saveAll(translations);
  }

  @Override
  @Transactional
  public void deleteAll(List<MessageTrl> messageTrls) {
    messageTrlRepository.deleteAll(messageTrls);
  }

  @Transactional
  @Override
  public void postUpdate(MessageTrl messageTrl) {
    var isAllTranslated = true;
    var message = messageTrl.getMessage();
    List<MessageTrl> trls = messageTrl.getMessage().getTranslations();
    for (MessageTrl trl : trls) {
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
    messageTrlUpdate
        .setMessageTrl(mapperFacade.map(messageTrl, org.jhapy.dto.domain.i18n.MessageTrl.class));
    messageTrlUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendMessageTrlUpdate(messageTrlUpdate);
  }

  @Override
  @Transactional
  public void postPersist(MessageTrl messageTrl) {
    var messageTrlUpdate = new I18NMessageTrlUpdate();
    messageTrlUpdate
        .setMessageTrl(mapperFacade.map(messageTrl, org.jhapy.dto.domain.i18n.MessageTrl.class));
    messageTrlUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendMessageTrlUpdate(messageTrlUpdate);
  }

  @Override
  @Transactional
  public void postRemove(MessageTrl messageTrl) {
    var messageTrlUpdate = new I18NMessageTrlUpdate();
    messageTrlUpdate
        .setMessageTrl(mapperFacade.map(messageTrl, org.jhapy.dto.domain.i18n.MessageTrl.class));
    messageTrlUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
    i18NQueue.sendMessageTrlUpdate(messageTrlUpdate);
  }

  @Override
  public JpaRepository<MessageTrl, Long> getRepository() {
    return messageTrlRepository;
  }

  @Transactional
  public synchronized void bootstrapMessages() {
    if (hasBootstrapped) {
      return;
    }

    if (!isBootstrapEnabled) {
      return;
    }

    String loggerPrefix = getLoggerPrefix("bootstrapMessages");
    try {
      importExcelFile(Files.readAllBytes(Path.of(bootstrapFile)));
    } catch (IOException e) {
      error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
  }

  @Transactional
  public String importExcelFile(byte[] content) {
    String loggerPrefix = getLoggerPrefix("importExcelFile");
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

          message = messageRepository.save(message);
        } else {
          message = optMessage.get();
          message.setCategory(category);
          message.setIsTranslated(true);

          debug(loggerPrefix, "Update : {0}", message);

          message = messageRepository.save(message);
        }

        Optional<MessageTrl> optMessageTrl = messageTrlRepository
            .getByMessageAndIso3Language(message, language);
        MessageTrl messageTrl;
        if (optMessageTrl.isEmpty()) {
          messageTrl = new MessageTrl();
          messageTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          messageTrl.setIso3Language(language);
          messageTrl.setMessage(message);
          messageTrl.setIsTranslated(true);

          debug(loggerPrefix, "Create Trl : {0}", messageTrl);

          messageTrlRepository.save(messageTrl);
        } else {
          messageTrl = optMessageTrl.get();
          if (!messageTrl.getIsTranslated()) {
            messageTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            messageTrl.setIso3Language(language);
            messageTrl.setMessage(message);
            messageTrl.setIsTranslated(true);

            debug(loggerPrefix, "Update Trl : {0}", messageTrl);

            messageTrlRepository.save(messageTrl);
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
}
