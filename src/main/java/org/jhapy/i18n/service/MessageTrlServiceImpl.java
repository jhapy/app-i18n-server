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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jhapy.commons.utils.HasLogger;
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

  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private Boolean isBootstrapEnabled;

  public MessageTrlServiceImpl(MessageRepository messageRepository,
      MessageTrlRepository messageTrlRepository) {
    this.messageRepository = messageRepository;
    this.messageTrlRepository = messageTrlRepository;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  protected void postLoad() {
    bootstrapMessages();
  }

  @Override
  public List<MessageTrl> findByMessage(Long messageId) {
    Optional<Message> _message = messageRepository.findById(messageId);
    if (_message.isPresent()) {
      return messageTrlRepository.findByMessage(_message.get());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public long countByMessage(Long messageId) {
    Optional<Message> _message = messageRepository.findById(messageId);
    if (_message.isPresent()) {
      return messageTrlRepository.countByMessage(_message.get());
    } else {
      return 0;
    }
  }

  @Transactional
  @Override
  public MessageTrl getByNameAndIso3Language(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getByNameAndIso3Language");

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Message> _message = messageRepository.getByName(name);
    Message message;
    if (!_message.isPresent()) {
      logger().warn(loggerPrefix + "Message '" + name + "' not found, create a new one");
      message = new Message();
      message.setName(name);
      message.setIsTranslated(false);
      message = messageRepository.save(message);
    } else {
      message = _message.get();
    }
    Optional<MessageTrl> _messageTrl = messageTrlRepository
        .getByMessageAndIso3Language(message, iso3Language);
    if (_messageTrl.isPresent()) {
      return _messageTrl.get();
    } else {
      logger().warn(loggerPrefix + "Message '" + name + "', '" + iso3Language
          + "' language translation not found, create a new one");
      MessageTrl messageTrl = new MessageTrl();
      messageTrl.setIso3Language(iso3Language);
      messageTrl.setMessage(message);

      Optional<MessageTrl> _defaultMessageTrl = messageTrlRepository
          .getByMessageAndIsDefault(message, true);
      if (_defaultMessageTrl.isPresent()) {
        messageTrl.setValue(_defaultMessageTrl.get().getValue());
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
    boolean isAllTranslated = true;
    Message message = messageTrl.getMessage();
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
  }

  @Override
  public JpaRepository<MessageTrl, Long> getRepository() {
    return messageTrlRepository;
  }

  @Transactional
  public synchronized void bootstrapMessages() {
    if (hasBootstrapped || !isBootstrapEnabled) {
      return;
    }
    String loggerPrefix = getLoggerPrefix("bootstrapMessages");
    try {
      importExcelFile(Files.readAllBytes(Path.of(bootstrapFile)));
    } catch (IOException e) {
      logger().error(loggerPrefix + "Something wrong happen : " + e.getMessage(), e);
    }
  }

  @Transactional
  public String importExcelFile(byte[] content) {
    String loggerPrefix = getLoggerPrefix("importExcelFile");
    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
      Sheet sheet = workbook.getSheet("Messages");
      if (sheet == null) {
        sheet = workbook.getSheet("messages");
      }

      logger().info(loggerPrefix + sheet.getPhysicalNumberOfRows() + " rows");

      Iterator<Row> rowIterator = sheet.rowIterator();
      int rowIndex = 0;

      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        rowIndex++;
        if (rowIndex == 1) {
          continue;
        }

        if (rowIndex % 10 == 0) {
          logger().info(loggerPrefix + "Handle row " + rowIndex);
        }

        int colIdx = 0;
        Cell categoryCell = row.getCell(colIdx++);
        Cell name0Cell = row.getCell(colIdx++);
        Cell name1Cell = row.getCell(colIdx++);
        Cell name2Cell = row.getCell(colIdx++);
        Cell name3Cell = row.getCell(colIdx++);
        Cell langCell = row.getCell(colIdx++);
        Cell valueCell = row.getCell(colIdx);

        if (langCell == null) {
          logger().error(loggerPrefix + "Empty value for language, skip");
          continue;
        }

        if (name0Cell == null) {
          logger().error(loggerPrefix + "Empty value for name, skip");
          continue;
        }

        String category = categoryCell == null ? null : categoryCell.getStringCellValue();

        String name = name0Cell.getStringCellValue();
        if (name1Cell != null && StringUtils.isNotBlank(name1Cell.getStringCellValue())) {
          name += "." + name1Cell.getStringCellValue();
        }
        if (name2Cell != null && StringUtils.isNotBlank(name2Cell.getStringCellValue())) {
          name += "." + name2Cell.getStringCellValue();
        }
        if (name3Cell != null && StringUtils.isNotBlank(name3Cell.getStringCellValue())) {
          name += "." + name3Cell.getStringCellValue();
        }

        String language = langCell.getStringCellValue();

        Optional<Message> _message = messageRepository.getByName(name);
        Message message;
        if (!_message.isPresent()) {
          message = new Message();
          message.setName(name);
          message.setCategory(category);
          message.setIsTranslated(true);

          logger().debug(loggerPrefix+"Create : " + message );

          message = messageRepository.save(message);
        } else {
          message = _message.get();
          message.setCategory(category);
          message.setIsTranslated(true);

          logger().debug(loggerPrefix+"Update : " + message );

          message = messageRepository.save(message);
        }

        Optional<MessageTrl> _messageTrl = messageTrlRepository
            .getByMessageAndIso3Language(message, language);
        MessageTrl messageTrl;
        if (!_messageTrl.isPresent()) {
          messageTrl = new MessageTrl();
          messageTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          messageTrl.setIso3Language(language);
          messageTrl.setMessage(message);
          messageTrl.setIsTranslated(true);

          logger().debug(loggerPrefix+"Create Trl : " + messageTrl );

          messageTrlRepository.save(messageTrl);
        } else {
          messageTrl = _messageTrl.get();
          if (!messageTrl.getIsTranslated()) {
            messageTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            messageTrl.setIso3Language(language);
            messageTrl.setMessage(message);
            messageTrl.setIsTranslated(true);

            logger().debug(loggerPrefix+"Update Trl : " + messageTrl );

            messageTrlRepository.save(messageTrl);
          }
        }
      }
    } catch (IOException e) {
      logger().error(loggerPrefix + "Something wrong happen : " + e.getMessage(), e);
      return e.getMessage();
    }

    logger().info(loggerPrefix + "Done");

    hasBootstrapped = true;

    return null;
  }
}
