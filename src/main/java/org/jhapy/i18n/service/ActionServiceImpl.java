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
import org.apache.poi.ss.usermodel.*;
import org.jhapy.dto.messageQueue.I18NActionTrlUpdate;
import org.jhapy.dto.messageQueue.I18NActionUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.converter.ActionConverter;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.jhapy.i18n.domain.*;
import org.jhapy.i18n.repository.ActionRepository;
import org.jhapy.i18n.repository.BaseRepository;
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
public class ActionServiceImpl implements ActionService {

  private final ActionRepository actionRepository;

  private final VersionRepository versionRepository;
  private final I18NQueue i18NQueue;
  private final EntityManager entityManager;
  private final ActionConverter actionConverter;
  private final ActionTrlConverter actionTrlConverter;
  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private Boolean isBootstrapEnabled;

  public ActionServiceImpl(
      ActionRepository actionRepository,
      VersionRepository versionRepository,
      ActionConverter actionConverter,
      I18NQueue i18NQueue,
      EntityManager entityManager,
      @Lazy ActionTrlConverter actionTrlConverter) {
    this.actionRepository = actionRepository;
    this.versionRepository = versionRepository;
    this.i18NQueue = i18NQueue;
    this.entityManager = entityManager;
    this.actionConverter = actionConverter;
    this.actionTrlConverter = actionTrlConverter;
  }

  @Override
  @Transactional
  public ActionTrl getActionTrlByActionIdAndLanguage(Long actionId, String iso3Language) {
    var action = actionRepository.getById(actionId);
    if (action.getTranslations().containsKey(iso3Language))
      return action.getTranslations().get(iso3Language);
    else {
      var optActionTrl =
          action.getTranslations().values().stream()
              .filter(actionTrl -> (actionTrl.getIsDefault() != null && actionTrl.getIsDefault()))
              .findFirst();
      var actionTrl = new ActionTrl();
      if (optActionTrl.isPresent()) {
        var defaultActionTrl = optActionTrl.get();
        actionTrl.setValue(defaultActionTrl.getValue());
        actionTrl.setTooltip(defaultActionTrl.getTooltip());
      } else {
        actionTrl.setValue(action.getName());
        actionTrl.setTooltip("");
      }
      actionTrl.setIsDefault(false);
      actionTrl.setIsTranslated(false);
      action.getTranslations().put(iso3Language, actionTrl);
      action = actionRepository.save(action);
      return action.getTranslations().get(iso3Language);
    }
  }

  @Override
  public List<ActionTrl> getActionTrlByIso3Language(String iso3Language) {
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    return actionRepository.findByIso3(iso3Language).stream()
        .map(
            actionTrlProjection -> {
              ActionTrl actionTrl = new ActionTrl();
              actionTrl.setName(actionTrlProjection.getName());
              actionTrl.setValue(actionTrlProjection.getValue());
              actionTrl.setIso3Language(iso3Language);
              actionTrl.setTooltip(actionTrlProjection.getTooltip());
              actionTrl.setIsDefault(actionTrlProjection.getIsDefault());
              actionTrl.setIsTranslated(actionTrlProjection.getIsTranslated());
              actionTrl.setRelatedEntityId(actionTrlProjection.getRelatedEntityId());
              return actionTrl;
            })
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public ActionTrl getByActionTrlNameAndLanguage(String name, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getByNameAndIso3Language", name, iso3Language);

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Action> _action = actionRepository.getByName(name);
    Action action;
    if (_action.isEmpty()) {
      logger().warn(loggerPrefix + "Action '" + name + "' not found, create a new one");
      action = new Action();
      action.setName(name);
      action.setIsTranslated(false);
      action = actionRepository.save(action);
    } else {
      action = _action.get();
    }
    if (action.getTranslations() != null && action.getTranslations().containsKey(iso3Language)) {
      return action.getTranslations().get(iso3Language);
    } else {
      logger()
          .warn(
              loggerPrefix
                  + "Action '"
                  + name
                  + "', '"
                  + iso3Language
                  + "' language translation not found, create a new one");
      if (action.getTranslations() == null) action.setTranslations(new HashMap<>());
      var optActionTrl =
          action.getTranslations().values().stream()
              .filter(EntityTranslationV2::getIsDefault)
              .findFirst();
      var actionTrl = new ActionTrl();
      if (optActionTrl.isPresent()) {
        var defaultActionTrl = optActionTrl.get();
        actionTrl.setValue(defaultActionTrl.getValue());
        actionTrl.setTooltip(defaultActionTrl.getTooltip());
      } else {
        actionTrl.setValue(name);
        actionTrl.setTooltip("");
      }
      actionTrl.setIsDefault(false);
      actionTrl.setIsTranslated(false);
      action.getTranslations().put(iso3Language, actionTrl);
      action = actionRepository.save(action);
      return action.getTranslations().get(iso3Language);
    }
  }

  @Override
  public List<ActionTrl> getActionTrls(Long actionId) {
    var action = actionRepository.getById(actionId);

    return action.getTranslations().keySet().stream()
        .map(
            key -> {
              var actionTrl = action.getTranslations().get(key);
              actionTrl.setRelatedEntityId(actionId);
              actionTrl.setIso3Language(key);
              return actionTrl;
            })
        .toList();
  }

  @Override
  @Transactional
  public void postUpdate(Action action) {
    if (hasBootstrapped()) {
      I18NActionUpdate actionUpdate = new I18NActionUpdate();
      actionUpdate.setAction(actionConverter.asDTO(action, null));
      actionUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
      i18NQueue.sendActionUpdate(actionUpdate);
    }
  }

  @Override
  @Transactional
  public void postPersist(Action action) {
    if (hasBootstrapped()) {
      I18NActionUpdate actionUpdate = new I18NActionUpdate();
      actionUpdate.setAction(actionConverter.asDTO(action, null));
      actionUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
      i18NQueue.sendActionUpdate(actionUpdate);
    }
  }

  @Override
  @Transactional
  public void postRemove(Action action) {
    if (hasBootstrapped()) {
      I18NActionUpdate actionUpdate = new I18NActionUpdate();
      actionUpdate.setAction(actionConverter.asDTO(action, null));
      actionUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
      i18NQueue.sendActionUpdate(actionUpdate);
    }
  }

  @Transactional
  @Override
  public void postUpdate(ActionTrl actionTrl) {
    var isAllTranslated = true;
    var action = actionRepository.getById(actionTrl.getRelatedEntityId());
    Map<String, ActionTrl> trls = action.getTranslations();
    for (ActionTrl trl : trls.values()) {
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
    var actionTrlUpdate = new I18NActionTrlUpdate();
    actionTrlUpdate.setActionTrl(actionTrlConverter.asDTO(actionTrl, null));
    actionTrlUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendActionTrlUpdate(actionTrlUpdate);
  }

  @Override
  @Transactional
  public void postPersist(ActionTrl actionTrl) {
    var actionTrlUpdate = new I18NActionTrlUpdate();
    actionTrlUpdate.setActionTrl(actionTrlConverter.asDTO(actionTrl, null));
    actionTrlUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendActionTrlUpdate(actionTrlUpdate);
  }

  @Override
  @Transactional
  public void postRemove(ActionTrl actionTrl) {
    var actionTrlUpdate = new I18NActionTrlUpdate();
    actionTrlUpdate.setActionTrl(actionTrlConverter.asDTO(actionTrl, null));
    actionTrlUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
    i18NQueue.sendActionTrlUpdate(actionTrlUpdate);
  }

  @Override
  @Transactional
  public void delete(Action entity) {
    ActionService.super.delete(entity);

    versionRepository.incActionRecords();
  }

  @Override
  public Specification<Action> buildSearchQuery(
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

      var result = new RelationalDbSpecification<Action>();
      result.or(criterias.toArray(new RelationalDbSearchCriteria[0]));
      return result;
    } else {
      return null;
    }
  }

  @Transactional
  @Override
  public void reset() {
    List<Action> allActions = actionRepository.findAll();
    allActions.forEach(
        action ->
            action
                .getTranslations()
                .values()
                .forEach(actionTrl -> actionTrl.setIsTranslated(false)));
    actionRepository.saveAll(allActions);
    versionRepository.incActionRecords();
  }

  @Override
  public boolean hasBootstrapped() {
    return hasBootstrapped;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  public void postLoad() {
    bootstrapActions();
  }

  @Transactional
  public synchronized void bootstrapActions() {
    if (hasBootstrapped) {
      return;
    }

    if (!isBootstrapEnabled) {
      hasBootstrapped = true;
      return;
    }

    var loggerPrefix = getLoggerPrefix("bootstrapActions");
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
    actionRepository.deleteAll();
    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {

      Sheet sheet = workbook.getSheet("Actions");
      if (sheet == null) {
        sheet = workbook.getSheet("actions");
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
        var name4Cell = row.getCell(colIdx++);
        var langCell = row.getCell(colIdx++);
        var valueCell = row.getCell(colIdx++);
        var tooltipCell = row.getCell(colIdx);

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
        if (name4Cell != null && StringUtils.isNotBlank(name4Cell.getStringCellValue())) {
          name += "." + name4Cell.getStringCellValue();
        }

        var language = langCell.getStringCellValue();

        Optional<Action> _action = actionRepository.getByName(name);
        Action action;
        if (_action.isEmpty()) {
          action = new Action();
          action.setName(name);
          action.setCategory(category);
          action.setIsTranslated(true);

          debug(loggerPrefix, "Create : {0}", action);

        } else {
          action = _action.get();
          action.setCategory(category);
          action.setIsTranslated(true);

          debug(loggerPrefix, "Update : {0}", action);
        }
        action = actionRepository.save(action);

        ActionTrl actionTrl = action.getTranslations().get(language);
        if (actionTrl == null) {
          actionTrl = new ActionTrl();
          actionTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          actionTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
          actionTrl.setIso3Language(language);
          actionTrl.setIsTranslated(true);

          logger().debug(loggerPrefix + "Create : " + actionTrl);
          action.getTranslations().put(language, actionTrl);
          actionRepository.save(action);
        } else {
          if (!actionTrl.getIsTranslated()) {
            actionTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            actionTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
            actionTrl.setIso3Language(language);
            actionTrl.setIsTranslated(true);

            logger().debug(loggerPrefix + "Update : " + actionTrl);

            action.getTranslations().put(language, actionTrl);
            actionRepository.save(action);
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

  @Override
  public BaseRepository<Action> getRepository() {
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