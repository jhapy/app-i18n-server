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
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.repository.ElementRepository;
import org.jhapy.i18n.repository.ElementTrlRepository;
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
public class ElementTrlServiceImpl implements ElementTrlService, HasLogger {

  private final ElementRepository elementRepository;
  private final ElementTrlRepository elementTrlRepository;

  private boolean hasBootstrapped = false;

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private Boolean isBootstrapEnabled;

  public ElementTrlServiceImpl(ElementRepository elementRepository,
      ElementTrlRepository elementTrlRepository) {
    this.elementRepository = elementRepository;
    this.elementTrlRepository = elementTrlRepository;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  protected void postLoad() {
    bootstrapElements();
  }

  @Override
  public List<ElementTrl> findByElement(Long elementId) {
    Optional<Element> _element = elementRepository.findById(elementId);
    if (_element.isPresent()) {
      return elementTrlRepository.findByElement(_element.get());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public long countByElement(Long elementId) {
    Optional<Element> _element = elementRepository.findById(elementId);
    if (_element.isPresent()) {
      return elementTrlRepository.countByElement(_element.get());
    } else {
      return 0;
    }
  }

  @Transactional
  @Override
  public ElementTrl getByNameAndIso3Language(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getByNameAndIso3Language", name, iso3Language);

    Assert.notNull(name, "Name mandatory");
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    Optional<Element> _element = elementRepository.getByName(name);
    Element element;
    if (!_element.isPresent()) {
      logger().warn(loggerPrefix + "Element '" + name + "' not found, create a new one");
      element = new Element();
      element.setName(name);
      element.setIsTranslated(false);
      element = elementRepository.save(element);
    } else {
      element = _element.get();
    }
    Optional<ElementTrl> _elementTrl = elementTrlRepository
        .getByElementAndIso3Language(element, iso3Language);
    if (_elementTrl.isPresent()) {
      return _elementTrl.get();
    } else {
      logger().warn(loggerPrefix + "Element '" + name + "', '" + iso3Language
          + "' language translation not found, create a new one");
      ElementTrl elementTrl = new ElementTrl();
      elementTrl.setIso3Language(iso3Language);
      elementTrl.setElement(element);

      Optional<ElementTrl> _defaultElementTrl = elementTrlRepository
          .getByElementAndIsDefault(element, true);
      if (_defaultElementTrl.isPresent()) {
        elementTrl.setValue(_defaultElementTrl.get().getValue());
      } else {
        elementTrl.setValue(name);
      }
      elementTrl.setIsTranslated(false);

      elementTrl = elementTrlRepository.save(elementTrl);
      return elementTrl;
    }
  }

  @Override
  public List<ElementTrl> getByIso3Language(String iso3Language) {
    Assert.notNull(iso3Language, "ISO3 language is mandatory");

    return elementTrlRepository.findByIso3Language(iso3Language);
  }

  @Override
  public List<ElementTrl> saveAll(List<ElementTrl> translations) {
    return elementTrlRepository.saveAll(translations);
  }

  @Override
  @Transactional
  public void deleteAll(List<ElementTrl> elementTrls) {
    elementTrlRepository.deleteAll(elementTrls);
  }

  @Transactional
  @Override
  public void postUpdate(ElementTrl elementTrl) {
    boolean isAllTranslated = true;
    Element element = elementTrl.getElement();
    List<ElementTrl> trls = element.getTranslations();
    for (ElementTrl trl : trls) {
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
  }

  @Override
  public JpaRepository<ElementTrl, Long> getRepository() {
    return elementTrlRepository;
  }

  @Transactional
  public synchronized void bootstrapElements() {
    if (hasBootstrapped || !isBootstrapEnabled) {
      return;
    }
    String loggerPrefix = getLoggerPrefix("bootstrapElements");
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

        Optional<Element> _element = elementRepository.getByName(name);
        Element element;
        if (!_element.isPresent()) {
          element = new Element();
          element.setName(name);
          element.setCategory(category);
          element.setIsTranslated(true);

          logger().debug(loggerPrefix + "Create : " + element);

          element = elementRepository.save(element);
        } else {
          element = _element.get();
          element.setCategory(category);
          element.setIsTranslated(true);

          logger().debug(loggerPrefix + "Update : " + element);

          element = elementRepository.save(element);
        }

        Optional<ElementTrl> _elementTrl = elementTrlRepository
            .getByElementAndIso3Language(element, language);
        ElementTrl elementTrl;
        if (!_elementTrl.isPresent()) {
          elementTrl = new ElementTrl();
          elementTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
          elementTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
          elementTrl.setIso3Language(language);
          elementTrl.setElement(element);
          elementTrl.setIsTranslated(true);

          logger().debug(loggerPrefix + "Create Trl : " + elementTrl);

          elementTrlRepository.save(elementTrl);
        } else {
          elementTrl = _elementTrl.get();
          if (!elementTrl.getIsTranslated()) {
            elementTrl.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
            elementTrl.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
            elementTrl.setIso3Language(language);
            elementTrl.setElement(element);
            elementTrl.setIsTranslated(true);

            logger().debug(loggerPrefix + "Update Trl : " + elementTrl);

            elementTrlRepository.save(elementTrl);
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
