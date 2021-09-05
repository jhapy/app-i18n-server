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
import org.jhapy.dto.messageQueue.I18NElementTrlUpdate;
import org.jhapy.dto.messageQueue.I18NElementUpdate;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.i18n.client.I18NQueue;
import org.jhapy.i18n.converter.ElementConverter;
import org.jhapy.i18n.converter.ElementTrlConverter;
import org.jhapy.i18n.domain.*;
import org.jhapy.i18n.repository.BaseRepository;
import org.jhapy.i18n.repository.ElementRepository;
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
public class ElementServiceImpl implements ElementService {

  private final ElementRepository elementRepository;
  private final VersionRepository versionRepository;
  private final I18NQueue i18NQueue;
  private final EntityManager entityManager;
  private final ElementConverter elementConverter;
  private final ElementTrlConverter elementTrlConverter;
  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private Boolean isBootstrapEnabled;

  public ElementServiceImpl(
      ElementRepository elementRepository,
      VersionRepository versionRepository,
      I18NQueue i18NQueue,
      EntityManager entityManager,
      ElementConverter elementConverter,
      @Lazy ElementTrlConverter elementTrlConverter) {
    this.elementRepository = elementRepository;
    this.versionRepository = versionRepository;
    this.elementConverter = elementConverter;
    this.i18NQueue = i18NQueue;
    this.entityManager = entityManager;
    this.elementTrlConverter = elementTrlConverter;
  }

  @Override
  @Transactional
  public ElementTrl getElementTrlByElementIdAndLanguage(Long elementId, String iso3Language) {
    var element = elementRepository.getById(elementId);
    if (element.getTranslations().containsKey(iso3Language))
      return element.getTranslations().get(iso3Language);
    else {
      var optElementTrl =
          element.getTranslations().values().stream()
              .filter(
                  elementTrl -> (elementTrl.getIsDefault() != null && elementTrl.getIsDefault()))
              .findFirst();
      var elementTrl = new ElementTrl();
      if (optElementTrl.isPresent()) {
        var defaultElementTrl = optElementTrl.get();
        elementTrl.setValue(defaultElementTrl.getValue());
        elementTrl.setTooltip(defaultElementTrl.getTooltip());
      } else {
        elementTrl.setValue(element.getName());
        elementTrl.setTooltip("");
      }
      elementTrl.setIsDefault(false);
      elementTrl.setIsTranslated(false);
      element = elementRepository.getById(elementId);
      element.getTranslations().put(iso3Language, elementTrl);
      element = elementRepository.save(element);
      return element.getTranslations().get(iso3Language);
    }
  }

  @Override
  public List<ElementTrl> getElementTrlByIso3Language(String iso3Language) {
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    return elementRepository.findByIso3(iso3Language).stream()
        .map(
            elementTrlProjection -> {
              ElementTrl elementTrl = new ElementTrl();
              elementTrl.setName(elementTrlProjection.getName());
              elementTrl.setValue(elementTrlProjection.getValue());
              elementTrl.setIso3Language(iso3Language);
              elementTrl.setTooltip(elementTrlProjection.getTooltip());
              elementTrl.setIsDefault(elementTrlProjection.getIsDefault());
              elementTrl.setIsTranslated(elementTrlProjection.getIsTranslated());
              elementTrl.setRelatedEntityId(elementTrlProjection.getRelatedEntityId());
              return elementTrl;
            })
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public ElementTrl getByElementTrlNameAndLanguage(String name, String iso3Language) {
    var loggerPrefix = getLoggerPrefix("getByNameAndIso3Language", name, iso3Language);

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Element> _element = elementRepository.getByName(name);
    Element element;
    if (_element.isEmpty()) {
      logger().warn(loggerPrefix + "Element '" + name + "' not found, create a new one");
      element = new Element();
      element.setName(name);
      element.setIsTranslated(false);
      element = elementRepository.save(element);
    } else {
      element = _element.get();
    }
    if (element.getTranslations() != null && element.getTranslations().containsKey(iso3Language)) {
      return element.getTranslations().get(iso3Language);
    } else {
      logger()
          .warn(
              loggerPrefix
                  + "Element '"
                  + name
                  + "', '"
                  + iso3Language
                  + "' language translation not found, create a new one");
      if (element.getTranslations() == null) element.setTranslations(new HashMap<>());
      var optElementTrl =
          element.getTranslations().values().stream()
              .filter(EntityTranslationV2::getIsDefault)
              .findFirst();
      var elementTrl = new ElementTrl();
      if (optElementTrl.isPresent()) {
        var defaultElementTrl = optElementTrl.get();
        elementTrl.setValue(defaultElementTrl.getValue());
        elementTrl.setTooltip(defaultElementTrl.getTooltip());
      } else {
        elementTrl.setValue(name);
        elementTrl.setTooltip("");
      }
      elementTrl.setIsDefault(false);
      elementTrl.setIsTranslated(false);
      element.getTranslations().put(iso3Language, elementTrl);
      element = elementRepository.save(element);
      var result = element.getTranslations().get(iso3Language);
      result.setRelatedEntityId(element.getId());
      return result;
    }
  }

  @Override
  public List<ElementTrl> getElementTrls(Long elementId) {
    var element = elementRepository.getById(elementId);

    return element.getTranslations().keySet().stream()
        .map(
            key -> {
              var elementTrl = element.getTranslations().get(key);
              elementTrl.setRelatedEntityId(elementId);
              elementTrl.setIso3Language(key);
              return elementTrl;
            })
        .toList();
  }

  @Override
  @Transactional
  public void postUpdate(Element element) {
    if (hasBootstrapped()) {
      I18NElementUpdate elementUpdate = new I18NElementUpdate();
      elementUpdate.setElement(elementConverter.asDTO(element, null));
      elementUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
      i18NQueue.sendElementUpdate(elementUpdate);
    }
  }

  @Override
  @Transactional
  public void postPersist(Element element) {
    if (hasBootstrapped()) {
      I18NElementUpdate elementUpdate = new I18NElementUpdate();
      elementUpdate.setElement(elementConverter.asDTO(element, null));
      elementUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
      i18NQueue.sendElementUpdate(elementUpdate);
    }
  }

  @Override
  @Transactional
  public void postRemove(Element element) {
    if (hasBootstrapped()) {
      I18NElementUpdate elementUpdate = new I18NElementUpdate();
      elementUpdate.setElement(elementConverter.asDTO(element, null));
      elementUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
      i18NQueue.sendElementUpdate(elementUpdate);
    }
  }

  @Transactional
  @Override
  public void postUpdate(ElementTrl elementTrl) {
    var isAllTranslated = true;
    var element = elementRepository.getById(elementTrl.getRelatedEntityId());
    Map<String, ElementTrl> trls = element.getTranslations();
    for (ElementTrl trl : trls.values()) {
      if (!trl.getIsTranslated()) {
        isAllTranslated = false;
        break;
      }
    }
    if (isAllTranslated && !element.getIsTranslated()) {
      element.setIsTranslated(true);
      elementRepository.save(element);
    } else if (!isAllTranslated && element.getIsTranslated()) {
      element.setIsTranslated(false);
      elementRepository.save(element);
    }
    var elementTrlUpdate = new I18NElementTrlUpdate();
    elementTrlUpdate.setElementTrl(elementTrlConverter.asDTO(elementTrl, null));
    elementTrlUpdate.setUpdateType(I18NUpdateTypeEnum.UPDATE);
    i18NQueue.sendElementTrlUpdate(elementTrlUpdate);
  }

  @Override
  @Transactional
  public void postPersist(ElementTrl elementTrl) {
    var elementTrlUpdate = new I18NElementTrlUpdate();
    elementTrlUpdate.setElementTrl(elementTrlConverter.asDTO(elementTrl, null));
    elementTrlUpdate.setUpdateType(I18NUpdateTypeEnum.INSERT);
    i18NQueue.sendElementTrlUpdate(elementTrlUpdate);
  }

  @Override
  @Transactional
  public void postRemove(ElementTrl elementTrl) {
    var elementTrlUpdate = new I18NElementTrlUpdate();
    elementTrlUpdate.setElementTrl(elementTrlConverter.asDTO(elementTrl, null));
    elementTrlUpdate.setUpdateType(I18NUpdateTypeEnum.DELETE);
    i18NQueue.sendElementTrlUpdate(elementTrlUpdate);
  }

  @Override
  @Transactional
  public void delete(Element entity) {
    ElementService.super.delete(entity);

    versionRepository.incElementRecords();
  }

  @Override
  public Specification<Element> buildSearchQuery(
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

      var result = new RelationalDbSpecification<Element>();
      result.or(criterias.toArray(new RelationalDbSearchCriteria[0]));
      return result;
    } else {
      return null;
    }
  }

  @Transactional
  @Override
  public void reset() {
    List<Element> allElements = elementRepository.findAll();
    allElements.forEach(
        element ->
            element
                .getTranslations()
                .values()
                .forEach(elementTrl -> elementTrl.setIsTranslated(false)));
    elementRepository.saveAll(allElements);
    versionRepository.incActionRecords();
  }

  @Override
  public boolean hasBootstrapped() {
    return hasBootstrapped;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  public void postLoad() {
    bootstrapElements();
  }

  @Transactional
  public synchronized void bootstrapElements() {
    if (hasBootstrapped) {
      return;
    }

    if (!isBootstrapEnabled) {
      hasBootstrapped = true;
      return;
    }

    var loggerPrefix = getLoggerPrefix("bootstrapElements");

    try {
      importExcelFile(Files.readAllBytes(Path.of(bootstrapFile)));
    } catch (IOException e) {
      logger().error(loggerPrefix + "Something wrong happen : " + e.getMessage(), e);
    }
  }

  @Transactional
  public String importExcelFile(byte[] content) {
    var loggerPrefix = getLoggerPrefix("importExcelFile");
    logger().info(loggerPrefix + "Clean data");
    elementRepository.deleteAll();
    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
      Sheet sheet = workbook.getSheet("Elements");
      if (sheet == null) {
        sheet = workbook.getSheet("elements");
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
        Cell name4Cell = row.getCell(colIdx++);
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
        if (name4Cell != null && StringUtils.isNotBlank(name4Cell.getStringCellValue())) {
          name += "." + name4Cell.getStringCellValue();
        }

        String language = langCell.getStringCellValue();

        Optional<Element> _element = elementRepository.getByName(name);
        Element element;
        if (_element.isEmpty()) {
          element = new Element();
          element.setName(name);
          element.setCategory(category);
          element.setIsTranslated(true);

          logger().debug(loggerPrefix + "Create : " + element);

        } else {
          element = _element.get();
          element.setCategory(category);
          element.setIsTranslated(true);

          logger().debug(loggerPrefix + "Update : " + element);
        }
        element = elementRepository.save(element);

        ElementTrl elementTrl = element.getTranslations().get(language);
        if (elementTrl == null) {
          elementTrl = new ElementTrl();
          elementTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          elementTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
          elementTrl.setIso3Language(language);

          elementTrl.setIsTranslated(true);

          logger().debug(loggerPrefix + "Create Trl : " + elementTrl);
          element.getTranslations().put(language, elementTrl);
          elementRepository.save(element);
        } else {
          if (!elementTrl.getIsTranslated()) {
            elementTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            elementTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
            elementTrl.setIso3Language(language);
            elementTrl.setIsTranslated(true);

            logger().debug(loggerPrefix + "Update Trl : " + elementTrl);

            element.getTranslations().put(language, elementTrl);
            elementRepository.save(element);
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
  public BaseRepository<Element> getRepository() {
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
}