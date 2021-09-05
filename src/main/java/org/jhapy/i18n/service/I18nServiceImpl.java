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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 11/04/2020
 */
@Transactional(readOnly = true)
@Service
public class I18nServiceImpl implements I18nService {

  private final ElementService elementService;
  private final ElementRepository elementRepository;

  private final MessageService messageService;
  private final MessageRepository messageRepository;

  private final ActionService actionService;
  private final ActionRepository actionRepository;

  private final VersionRepository versionRepository;

  private static final String[] i18nExportHeaders =
      new String[] {
        "Cat", "Name0", "Name1", "Name2", "Name3", "Name4", "Language", "Value", "Tooltip", "Key"
      };
  private static final String[] i18nExportMessageHeaders =
      new String[] {"Cat", "Name0", "Name1", "Name2", "Name3", "Language", "Value", "Key"};

  public I18nServiceImpl(
      ElementService elementService,
      ElementRepository elementRepository,
      MessageService messageService,
      MessageRepository messageRepository,
      ActionService actionService,
      ActionRepository actionRepository,
      VersionRepository versionRepository) {
    this.elementService = elementService;
    this.elementRepository = elementRepository;
    this.messageService = messageService;
    this.messageRepository = messageRepository;
    this.actionService = actionService;
    this.actionRepository = actionRepository;
    this.versionRepository = versionRepository;
  }

  @Override
  public Integer getElementCurrentVersion(String isoLang) {
    return versionRepository.getElementCurrentVersion(isoLang);
  }

  @Override
  public Integer getMessageCurrentVersion(String isoLang) {
    return versionRepository.getMessageCurrentVersion(isoLang);
  }

  @Override
  public Integer getActionCurrentVersion(String isoLang) {
    return versionRepository.getActionCurrentVersion(isoLang);
  }

  public List<String> getExistingLanguages() {
    return actionRepository.getIso3Languages();
  }

  private static Map<String, CellStyle> createStyles(Workbook wb) {
    Map<String, CellStyle> styles = new HashMap<>();

    CellStyle style;
    var headerFont = wb.createFont();
    headerFont.setFontName("Calibri");
    headerFont.setFontHeightInPoints((short) 12);
    headerFont.setBold(true);
    style = wb.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setFont(headerFont);
    styles.put("header", style);

    var defaultFont = wb.createFont();
    defaultFont.setFontName("Calibri");
    defaultFont.setFontHeightInPoints((short) 12);
    style = wb.createCellStyle();
    style.setFont(defaultFont);
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setWrapText(true);
    styles.put("cell_normal", style);

    return styles;
  }

  public Byte[] getI18NFile() {
    Workbook wb = new XSSFWorkbook();

    Map<String, CellStyle> styles = createStyles(wb);
    {
      var sheet = wb.createSheet("Elements");

      var headerRow = sheet.createRow(0);
      headerRow.setHeightInPoints(12.75f);
      for (var i = 0; i < i18nExportHeaders.length; i++) {
        var cell = headerRow.createCell(i);
        cell.setCellValue(i18nExportHeaders[i]);
        cell.setCellStyle(styles.get("header"));
      }

      sheet.createFreezePane(0, 1);

      Row row;
      Cell cell;
      var rownum = 1;
      List<Element> elementList =
          elementRepository.findAll(Sort.by(Order.asc("category"), Order.asc("name")));
      List<String> iso3Languages = actionRepository.getIso3Languages();

      for (Element element : elementList) {
        for (String iso3Language : iso3Languages) {
          row = sheet.createRow(rownum);

          var j = 0;
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(element.getCategory());

          String[] nameSplited = element.getName().split("\\.");
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(nameSplited[0]);

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 1) {
            cell.setCellValue(nameSplited[1]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 2) {
            cell.setCellValue(nameSplited[2]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 3) {
            cell.setCellValue(nameSplited[3]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 4) {
            cell.setCellValue(nameSplited[4]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(iso3Language);

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          var elementTrl =
              elementService.getElementTrlByElementIdAndLanguage(element.getId(), iso3Language);
          if (elementTrl != null && !elementTrl.getValue().equals(element.getName())) {
            cell.setCellValue(elementTrl.getValue());
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (elementTrl != null && !elementTrl.getValue().equals(element.getName())) {
            cell.setCellValue(elementTrl.getTooltip());
          }

          rownum++;
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_g"));
          cell.setCellFormula(
              "B"
                  + rownum
                  + "&IF(C"
                  + rownum
                  + "<>\"\",\".\"&C"
                  + rownum
                  + ",\"\")&IF(D"
                  + rownum
                  + "<>\"\",\".\"&D"
                  + rownum
                  + ",\"\")&IF(E"
                  + rownum
                  + "<>\"\",\".\"&E"
                  + rownum
                  + ",\"\")");
        }
      }
      sheet.setColumnWidth(0, 256 * 10);
      sheet.setColumnWidth(1, 256 * 15);
      sheet.setColumnWidth(2, 256 * 25);
      sheet.setColumnWidth(3, 256 * 25);
      sheet.setColumnWidth(4, 256 * 25);
      sheet.setColumnWidth(5, 256 * 10);
      sheet.setColumnWidth(6, 256 * 65);
      sheet.setColumnWidth(7, 256 * 65);
      sheet.setColumnWidth(8, 256 * 45);
    }
    {
      var sheet = wb.createSheet("Actions");

      var headerRow = sheet.createRow(0);
      headerRow.setHeightInPoints(12.75f);
      for (var i = 0; i < i18nExportHeaders.length; i++) {
        var cell = headerRow.createCell(i);
        cell.setCellValue(i18nExportHeaders[i]);
        cell.setCellStyle(styles.get("header"));
      }

      sheet.createFreezePane(0, 1);

      Row row;
      Cell cell;
      var rownum = 1;
      List<Action> actionList =
          actionRepository.findAll(Sort.by(Order.asc("category"), Order.asc("name")));
      List<String> iso3Languages = actionRepository.getIso3Languages();

      for (Action action : actionList) {
        for (String iso3Language : iso3Languages) {
          row = sheet.createRow(rownum);

          var j = 0;
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(action.getCategory());

          String[] nameSplited = action.getName().split("\\.");
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(nameSplited[0]);

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 1) {
            cell.setCellValue(nameSplited[1]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 2) {
            cell.setCellValue(nameSplited[2]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 3) {
            cell.setCellValue(nameSplited[3]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 4) {
            cell.setCellValue(nameSplited[4]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(iso3Language);

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          var actionTrl =
              actionService.getActionTrlByActionIdAndLanguage(action.getId(), iso3Language);
          if (actionTrl != null && !actionTrl.getValue().equals(action.getName())) {
            cell.setCellValue(actionTrl.getValue());
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (actionTrl != null && !actionTrl.getValue().equals(action.getName())) {
            cell.setCellValue(actionTrl.getTooltip());
          }

          rownum++;
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_g"));
          cell.setCellFormula(
              "B"
                  + rownum
                  + "&IF(C"
                  + rownum
                  + "<>\"\",\".\"&C"
                  + rownum
                  + ",\"\")&IF(D"
                  + rownum
                  + "<>\"\",\".\"&D"
                  + rownum
                  + ",\"\")&IF(E"
                  + rownum
                  + "<>\"\",\".\"&E"
                  + rownum
                  + ",\"\")");
        }
      }
      sheet.setColumnWidth(0, 256 * 10);
      sheet.setColumnWidth(1, 256 * 15);
      sheet.setColumnWidth(2, 256 * 25);
      sheet.setColumnWidth(3, 256 * 25);
      sheet.setColumnWidth(4, 256 * 25);
      sheet.setColumnWidth(5, 256 * 10);
      sheet.setColumnWidth(6, 256 * 65);
      sheet.setColumnWidth(7, 256 * 65);
      sheet.setColumnWidth(8, 256 * 45);
    }
    {
      var sheet = wb.createSheet("Messages");

      var headerRow = sheet.createRow(0);
      headerRow.setHeightInPoints(12.75f);
      for (var i = 0; i < i18nExportMessageHeaders.length; i++) {
        var cell = headerRow.createCell(i);
        cell.setCellValue(i18nExportMessageHeaders[i]);
        cell.setCellStyle(styles.get("header"));
      }

      sheet.createFreezePane(0, 1);

      Row row;
      Cell cell;
      var rownum = 1;
      List<Message> messageList =
          messageRepository.findAll(Sort.by(Order.asc("category"), Order.asc("name")));
      List<String> iso3Languages = actionRepository.getIso3Languages();

      for (Message message : messageList) {
        for (String iso3Language : iso3Languages) {
          row = sheet.createRow(rownum);

          var j = 0;
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(message.getCategory());

          String[] nameSplited = message.getName().split("\\.");
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(nameSplited[0]);

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 1) {
            cell.setCellValue(nameSplited[1]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 2) {
            cell.setCellValue(nameSplited[2]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (nameSplited.length > 3) {
            cell.setCellValue(nameSplited[3]);
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          cell.setCellValue(iso3Language);

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          var messageTrl =
              messageService.getMessageTrlByMessageIdAndLanguage(message.getId(), iso3Language);
          if (messageTrl != null && !messageTrl.getValue().equals(message.getName())) {
            cell.setCellValue(messageTrl.getValue());
          }
          rownum++;
          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_g"));
          cell.setCellFormula(
              "B"
                  + rownum
                  + "&IF(C"
                  + rownum
                  + "<>\"\",\".\"&C"
                  + rownum
                  + ",\"\")&IF(D"
                  + rownum
                  + "<>\"\",\".\"&D"
                  + rownum
                  + ",\"\")&IF(E"
                  + rownum
                  + "<>\"\",\".\"&E"
                  + rownum
                  + ",\"\")");
        }
      }
      sheet.setColumnWidth(0, 256 * 10);
      sheet.setColumnWidth(1, 256 * 15);
      sheet.setColumnWidth(2, 256 * 25);
      sheet.setColumnWidth(3, 256 * 25);
      sheet.setColumnWidth(4, 256 * 25);
      sheet.setColumnWidth(5, 256 * 10);
      sheet.setColumnWidth(6, 256 * 65);
      sheet.setColumnWidth(7, 256 * 45);
    }
    var outputStream = new ByteArrayOutputStream();
    try {
      wb.write(outputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ArrayUtils.toObject(outputStream.toByteArray());
  }

  @Transactional
  public String importI18NFile(Byte[] fileToImport) {
    byte[] fileContent = ArrayUtils.toPrimitive(fileToImport);
    actionService.reset();
    String fileImportResult = actionService.importExcelFile(fileContent);
    if (fileImportResult != null) {
      return fileImportResult;
    }

    elementService.reset();
    fileImportResult = elementService.importExcelFile(fileContent);
    if (fileImportResult != null) {
      return fileImportResult;
    }

    messageService.reset();
    fileImportResult = messageService.importExcelFile(fileContent);
    return fileImportResult;
  }
}