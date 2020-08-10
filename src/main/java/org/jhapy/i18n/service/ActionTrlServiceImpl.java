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
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.repository.ActionRepository;
import org.jhapy.i18n.repository.ActionTrlRepository;
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
public class ActionTrlServiceImpl implements ActionTrlService, HasLogger {

  private final ActionRepository actionRepository;
  private final ActionTrlRepository actionTrlRepository;

  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private Boolean isBootstrapEnabled;

  public ActionTrlServiceImpl(ActionRepository actionRepository,
      ActionTrlRepository actionTrlRepository) {
    this.actionRepository = actionRepository;
    this.actionTrlRepository = actionTrlRepository;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  protected void postLoad() {
    bootstrapActions();
  }

  @Override
  public List<ActionTrl> findByAction(Long actionId) {
    Optional<Action> _action = actionRepository.findById(actionId);
    if (_action.isPresent()) {
      return actionTrlRepository.findByAction(_action.get());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public long countByAction(Long actionId) {
    Optional<Action> _action = actionRepository.findById(actionId);
    if (_action.isPresent()) {
      return actionTrlRepository.countByAction(_action.get());
    } else {
      return 0;
    }
  }

  @Transactional
  @Override
  public ActionTrl getByNameAndIso3Language(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getByNameAndIso3Language");

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Action> _action = actionRepository.getByName(name);
    Action action;
    if (!_action.isPresent()) {
      logger().warn(loggerPrefix + "Action '" + name + "' not found, create a new one");
      action = new Action();
      action.setName(name);
      action.setIsTranslated(false);
      action = actionRepository.save(action);
    } else {
      action = _action.get();
    }
    Optional<ActionTrl> _actionTrl = actionTrlRepository
        .getByActionAndIso3Language(action, iso3Language);
    if (_actionTrl.isPresent()) {
      return _actionTrl.get();
    } else {
      logger().warn(loggerPrefix + "Action '" + name + "', '" + iso3Language
          + "' language translation not found, create a new one");
      ActionTrl actionTrl = new ActionTrl();
      actionTrl.setIso3Language(iso3Language);
      actionTrl.setAction(action);

      Optional<ActionTrl> _defaultActionTrl = actionTrlRepository
          .getByActionAndIsDefault(action, true);
      if (_defaultActionTrl.isPresent()) {
        actionTrl.setValue(_defaultActionTrl.get().getValue());
      } else {
        actionTrl.setValue(name);
      }
      actionTrl.setIsTranslated(false);

      actionTrl = actionTrlRepository.save(actionTrl);
      return actionTrl;
    }
  }

  @Override
  public List<ActionTrl> getByIso3Language(String iso3Language) {
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    return actionTrlRepository.findByIso3Language(iso3Language);
  }

  @Override
  public List<ActionTrl> saveAll(List<ActionTrl> translations) {
    return actionTrlRepository.saveAll(translations);
  }

  @Override
  @Transactional
  public void deleteAll(List<ActionTrl> actionTrls) {
    actionTrlRepository.deleteAll(actionTrls);
  }

  @Override
  public JpaRepository<ActionTrl, Long> getRepository() {
    return actionTrlRepository;
  }

  @Transactional
  @Override
  public void postUpdate(ActionTrl actionTrl) {
    boolean isAllTranslated = true;
    Action action = actionTrl.getAction();
    List<ActionTrl> trls = actionTrl.getAction().getTranslations();
    for (ActionTrl trl : trls) {
      if (!trl.getIsTranslated()) {
        isAllTranslated = false;
        break;
      }
    }
    if (isAllTranslated && !action.getIsTranslated()) {
      action.setIsTranslated(true);
      actionRepository.save(action);
    } else if (!isAllTranslated && action.getIsTranslated()) {
      action.setIsTranslated(false);
      actionRepository.save(action);
    }
  }

  @Transactional
  public synchronized void bootstrapActions() {
    if (hasBootstrapped || !isBootstrapEnabled) {
      return;
    }

    String loggerPrefix = getLoggerPrefix("bootstrapActions");

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

      Sheet sheet = workbook.getSheet("Actions");
      if (sheet == null) {
        sheet = workbook.getSheet("actions");
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
        Cell valueCell = row.getCell(colIdx++);
        Cell tooltipCell = row.getCell(colIdx);

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

        Optional<Action> _action = actionRepository.getByName(name);
        Action action;
        if (!_action.isPresent()) {
          action = new Action();
          action.setName(name);
          action.setCategory(category);
          action.setIsTranslated(true);
          action = actionRepository.save(action);
        } else {
          action = _action.get();
          action.setCategory(category);
          action.setIsTranslated(true);
          action = actionRepository.save(action);
        }

        Optional<ActionTrl> _actionTrl = actionTrlRepository
            .getByActionAndIso3Language(action, language);
        ActionTrl actionTrl;
        if (!_actionTrl.isPresent()) {
          actionTrl = new ActionTrl();
          actionTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          actionTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
          actionTrl.setIso3Language(language);
          actionTrl.setAction(action);
          actionTrl.setIsTranslated(true);

          actionTrlRepository.save(actionTrl);
        } else {
          actionTrl = _actionTrl.get();
          if (!actionTrl.getIsTranslated()) {
            actionTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            actionTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
            actionTrl.setIso3Language(language);
            actionTrl.setAction(action);
            actionTrl.setIsTranslated(true);

            actionTrlRepository.save(actionTrl);
          }
        }
      }
    } catch (Throwable e) {
      logger().error(loggerPrefix + "Something wrong happen : " + e.getMessage(), e);
      return e.getMessage();
    }

    logger().info(loggerPrefix + "Done");

    hasBootstrapped = true;
    return null;
  }
}
