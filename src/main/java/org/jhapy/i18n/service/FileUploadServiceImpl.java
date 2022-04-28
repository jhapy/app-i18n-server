package org.jhapy.i18n.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.cqrs.command.SubmitUploadCommand;
import org.jhapy.cqrs.command.i18n.*;
import org.jhapy.cqrs.query.i18n.*;
import org.jhapy.dto.domain.i18n.*;
import org.jhapy.dto.serviceResponse.FileUploadStatusResponse;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.FileUpload;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.errorHandeling.FileValidationError;
import org.jhapy.i18n.query.ActionQueryHandler;
import org.jhapy.i18n.query.ActionTrlQueryHandler;
import org.jhapy.i18n.query.ElementTrlQueryHandler;
import org.jhapy.i18n.query.MessageTrlQueryHandler;
import org.jhapy.i18n.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
  private final FileUploadRepository repository;
  private final EntityManager entityManager;
  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;
  private final ElementLookupRepository elementLookupRepository;
private final ElementRepository elementRepository;
private final ElementTrlRepository elementTrlRepository;
private final ActionRepository actionRepository;
private final ActionTrlRepository actionTrlRepository;
private final MessageRepository messageRepository;
private final MessageTrlRepository messageTrlRepository;

private final ElementTrlQueryHandler elementTrlQueryHandler;
private final ActionTrlQueryHandler actionTrlQueryHandler;
private final MessageTrlQueryHandler messageTrlQueryHandler;

  private boolean hasBootstrapped = false;

  private static final String[] i18nExportHeaders =
          new String[] {
                  "Cat", "Name0", "Name1", "Name2", "Name3", "Name4", "Language", "Value", "Tooltip", "Key"
          };
  private static final String[] i18nExportMessageHeaders =
          new String[] {"Cat", "Name0", "Name1", "Name2", "Name3", "Language", "Value", "Key"};

  @Value("${jhapy.bootstrap.i18n.file}")
  private String bootstrapFile;

  @Value("${jhapy.bootstrap.i18n.enabled}")
  private boolean isBootstrapEnabled;

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
              elementRepository.findAll(Sort.by(Sort.Order.asc("category"), Sort.Order.asc("name")));
      List<String> iso3Languages = elementTrlRepository.getDistinctIso3Language();

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
          var elementTrl =elementTrlQueryHandler.getElementTrlByElementIdAndIso3Language( new GetElementTrlByElementIdAndIso3LanguageQuery(element.getId(), iso3Language));
          if (elementTrl != null && elementTrl.getData() != null && !elementTrl.getData().getValue().equals(element.getName())) {
            cell.setCellValue(elementTrl.getData().getValue());
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (elementTrl != null && elementTrl.getData() != null && !elementTrl.getData().getValue().equals(element.getName())) {
            cell.setCellValue(elementTrl.getData().getTooltip());
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
              actionRepository.findAll(Sort.by(Sort.Order.asc("category"), Sort.Order.asc("name")));
      List<String> iso3Languages = actionTrlRepository.getDistinctIso3Language();

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
          var actionTrl =actionTrlQueryHandler.getActionTrlByActionIdAndIso3Language(new GetActionTrlByActionIdAndIso3LanguageQuery(action.getId(), iso3Language));
          if (actionTrl != null && actionTrl.getData() != null && !actionTrl.getData().getValue().equals(action.getName())) {
            cell.setCellValue(actionTrl.getData().getValue());
          }

          cell = row.createCell(j++);
          cell.setCellStyle(styles.get("cell_normal"));
          if (actionTrl != null && actionTrl.getData() != null &&  !actionTrl.getData().getValue().equals(action.getName())) {
            cell.setCellValue(actionTrl.getData().getTooltip());
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
              messageRepository.findAll(Sort.by(Sort.Order.asc("category"), Sort.Order.asc("name")));
      List<String> iso3Languages = messageTrlRepository.getDistinctIso3Language();

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
                  messageTrlQueryHandler.getMessageTrlByMessageIdAndIso3Language(new GetMessageTrlByMessageIdAndIso3LanguageQuery(message.getId(), iso3Language));
          if (messageTrl != null && messageTrl.getData() != null &&  !messageTrl.getData().getValue().equals(message.getName())) {
            cell.setCellValue(messageTrl.getData().getValue());
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

  @Override
  public UUID uploadFile(String filename, Byte[] fileContent) {
    var fileUpload = new FileUpload();
    fileUpload.setFilename(filename);
    fileUpload.setFileContent(ArrayUtils.toPrimitive(fileContent));
    return repository.save(fileUpload).getId();
  }

  @Override
  public FileUploadStatusResponse getFileUploadStatus(UUID id) {
    FileUpload fileUpload;

    var result = repository.findById(id);
    if (result.isPresent()) fileUpload = result.get();
    else {
      var fileUploadStatusResponse = new FileUploadStatusResponse();
      fileUploadStatusResponse.setUploadId(id);
      fileUploadStatusResponse.setIsValidated(false);
      fileUploadStatusResponse.setIsImported(false);
      fileUploadStatusResponse.setErrorMessage("File Upload not found");
      return fileUploadStatusResponse;
    }

    var fileUploadStatusResponse = new FileUploadStatusResponse();
    fileUploadStatusResponse.setUploadId(id);
    fileUploadStatusResponse.setIsImported(fileUpload.getIsImported());
    fileUploadStatusResponse.setIsValidated(fileUpload.getIsValidated());
    fileUploadStatusResponse.setErrorMessage(fileUploadStatusResponse.getErrorMessage());
    return fileUploadStatusResponse;
  }

  public void validate(UUID id) {
    var loggerPrefix = getLoggerPrefix("validateExcelFile");

    var fileUpload = repository.getById(id);

    var errorMessages = new StringBuilder();

    try (var workbook =
        WorkbookFactory.create(new ByteArrayInputStream(fileUpload.getFileContent()))) {
      var sheet = workbook.getSheet("Elements");
      if (sheet == null) {
        sheet = workbook.getSheet("elements");
      }

      errorMessages.append(validateElementSheet(sheet));

      sheet = workbook.getSheet("Actions");
      if (sheet == null) {
        sheet = workbook.getSheet("actions");
      }
      errorMessages.append(validateActionSheet(sheet));

      sheet = workbook.getSheet("Messages");
      if (sheet == null) {
        sheet = workbook.getSheet("messages");
      }
      errorMessages.append(validateMessageSheet(sheet));

    } catch (Throwable e) {
      errorMessages.append(String.format("Something wrong happen %s%n", e.getMessage()));
      logger().error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
    if (errorMessages.length() > 0) {
      fileUpload.setIsValidated(false);
      fileUpload.setErrorMessage(errorMessages.toString());
      repository.save(fileUpload);
      throw new FileValidationError(errorMessages.toString());
    }
    fileUpload.setIsValidated(true);
    repository.save(fileUpload);
    info(loggerPrefix, "Done");
  }

  protected String validateElementSheet(Sheet sheet) {
    var loggerPrefix = getLoggerPrefix("validateElementSheet");

    info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

    var rowIterator = sheet.rowIterator();
    var rowIndex = 0;
    var errorMessages = new StringBuilder();
    try {
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
          errorMessages.append(String.format("Line %d, missing language%n", rowIndex));
          error(loggerPrefix, "Empty value for language, skip");
          continue;
        }

        if (name0Cell == null) {
          errorMessages.append(String.format("Line %d, missing name%n", rowIndex));
          error(loggerPrefix, "Empty value for name, skip");
        }
      }
    } catch (Throwable e) {
      errorMessages.append(String.format("Line %d, unknown error %s%n", rowIndex, e.getMessage()));
      logger().error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
    return errorMessages.toString();
  }

  protected String validateActionSheet(Sheet sheet) {
    var loggerPrefix = getLoggerPrefix("validateActionSheet");

    info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

    var rowIterator = sheet.rowIterator();
    var rowIndex = 0;
    var errorMessages = new StringBuilder();
    try {
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
          errorMessages.append(String.format("Line %d, missing language%n", rowIndex));
          error(loggerPrefix, "Empty value for language, skip");
          continue;
        }

        if (name0Cell == null) {
          errorMessages.append(String.format("Line %d, missing name%n", rowIndex));
          error(loggerPrefix, "Empty value for name, skip");
        }
      }
    } catch (Throwable e) {
      errorMessages.append(String.format("Line %d, unknown error %s%n", rowIndex, e.getMessage()));
      logger().error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
    return errorMessages.toString();
  }

  protected String validateMessageSheet(Sheet sheet) {
    var loggerPrefix = getLoggerPrefix("validateMessageSheet");

    info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

    var rowIterator = sheet.rowIterator();
    var rowIndex = 0;
    var errorMessages = new StringBuilder();
    try {
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

        if (langCell == null) {
          errorMessages.append(String.format("Line %d, missing language%n", rowIndex));
          error(loggerPrefix, "Empty value for language, skip");
          continue;
        }

        if (name0Cell == null) {
          errorMessages.append(String.format("Line %d, missing name%n", rowIndex));
          error(loggerPrefix, "Empty value for name, skip");
        }
      }
    } catch (Throwable e) {
      errorMessages.append(String.format("Line %d, unknown error %s%n", rowIndex, e.getMessage()));
      logger().error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
    return errorMessages.toString();
  }

  @Override
  public void cleanDatabase() {
    var loggerPrefix = getLoggerPrefix("cleanDatabase");

    List<CompletableFuture<UUID>> deletingList = new ArrayList<>();

    GetAllElementsResponse existingElements =
        queryGateway.query(new GetAllElementsQuery(), GetAllElementsResponse.class).join();
    existingElements
        .getData()
        .forEach(
            elementDTO ->
                deletingList.add(
                    commandGateway.send(new DeleteElementCommand(elementDTO.getId()))));

    GetAllActionsResponse existingActions =
        queryGateway.query(new GetAllActionsQuery(), GetAllActionsResponse.class).join();
    existingActions
        .getData()
        .forEach(
            actionDTO ->
                deletingList.add(commandGateway.send(new DeleteActionCommand(actionDTO.getId()))));

    GetAllMessagesResponse existingMessages =
        queryGateway.query(new GetAllMessagesQuery(), GetAllMessagesResponse.class).join();
    existingMessages
        .getData()
        .forEach(
            messageDTO ->
                deletingList.add(
                    commandGateway.send(new DeleteMessageCommand(messageDTO.getId()))));

    debug(loggerPrefix, "Wait for completion");
    CompletableFuture.allOf(deletingList.toArray(new CompletableFuture[0])).join();
  }

  public void importFile(UUID id) throws IOException {
    var loggerPrefix = getLoggerPrefix("importElementsFromExcel");

    var fileUpload = repository.getById(id);

    Map<String, ElementDTO> elements;
    Map<String, ActionDTO> actions;
    Map<String, MessageDTO> messages;

    try (var workbook =
        WorkbookFactory.create(new ByteArrayInputStream(fileUpload.getFileContent()))) {
      var sheet = workbook.getSheet("Elements");
      if (sheet == null) {
        sheet = workbook.getSheet("elements");
      }
      elements = importElementSheet(sheet);

      sheet = workbook.getSheet("Actions");
      if (sheet == null) {
        sheet = workbook.getSheet("actions");
      }
      actions = importActionSheet(sheet);

      sheet = workbook.getSheet("Messages");
      if (sheet == null) {
        sheet = workbook.getSheet("messages");
      }
      messages = importMessageSheet(sheet);
    }

    List<CompletableFuture<UUID>> savingList = new ArrayList<>();
    elements
        .values()
        .forEach(
            elementDTO ->
                savingList.add(commandGateway.send(new CreateElementCommand(elementDTO))));
    actions
        .values()
        .forEach(
            actionDTO -> savingList.add(commandGateway.send(new CreateActionCommand(actionDTO))));

    messages
        .values()
        .forEach(
            messageDTO ->
                savingList.add(commandGateway.send(new CreateMessageCommand(messageDTO))));

    debug(loggerPrefix, "Wait for completion");
    var allFuturesResult = CompletableFuture.allOf(savingList.toArray(new CompletableFuture[0]));
    allFuturesResult.whenComplete(
        (unused, throwable) -> {
          if (throwable == null) fileUpload.setIsImported(true);
          else {
            fileUpload.setIsImported(false);
            fileUpload.setErrorMessage(throwable.getMessage());
          }
          repository.save(fileUpload);
        });

    info(loggerPrefix, "Done");
  }

  protected Map<String, ElementDTO> importElementSheet(Sheet sheet) {
    var loggerPrefix = getLoggerPrefix("importElementSheet");

    Map<String, ElementDTO> elements = new HashMap<>();

    info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

    var rowIterator = sheet.rowIterator();
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

      var category = categoryCell == null ? null : categoryCell.getStringCellValue();

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

      ElementDTO elementDTO;
      if (elements.containsKey(name)) {
        elementDTO = elements.get(name);
      } else {
        elementDTO = new ElementDTO();
        elementDTO.setName(name);
        elementDTO.setCategory(category);
        elementDTO.setTranslated(true);

        elements.put(name, elementDTO);
      }

      var elementTrlDTO = new ElementTrlDTO();
      elementTrlDTO.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
      elementTrlDTO.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
      elementTrlDTO.setIso3Language(language);
      elementTrlDTO.setParentId(elementDTO.getId());
      elementTrlDTO.setTranslated(true);
      elementDTO.getTranslations().add(elementTrlDTO);
    }

    return elements;
  }

  protected Map<String, ActionDTO> importActionSheet(Sheet sheet) {
    var loggerPrefix = getLoggerPrefix("importActionSheet");

    Map<String, ActionDTO> actions = new HashMap<>();

    info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

    var rowIterator = sheet.rowIterator();
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

      var category = categoryCell == null ? null : categoryCell.getStringCellValue();

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

      ActionDTO actionDTO;
      if (actions.containsKey(name)) {
        actionDTO = actions.get(name);
      } else {
        actionDTO = new ActionDTO();
        actionDTO.setName(name);
        actionDTO.setCategory(category);
        actionDTO.setTranslated(true);

        actions.put(name, actionDTO);
      }

      var actionTrlDTO = new ActionTrlDTO();
      actionTrlDTO.setId(UUID.randomUUID());
      actionTrlDTO.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
      actionTrlDTO.setTooltip(tooltipCell == null ? null : tooltipCell.getStringCellValue());
      actionTrlDTO.setIso3Language(language);
      actionTrlDTO.setParentId(actionDTO.getId());
      actionTrlDTO.setTranslated(true);
      actionDTO.getTranslations().add(actionTrlDTO);
    }

    return actions;
  }

  protected Map<String, MessageDTO> importMessageSheet(Sheet sheet) {
    var loggerPrefix = getLoggerPrefix("importMessageSheet");

    Map<String, MessageDTO> messages = new HashMap<>();

    info(loggerPrefix, "{0} rows", sheet.getPhysicalNumberOfRows());

    var rowIterator = sheet.rowIterator();
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

      var category = categoryCell == null ? null : categoryCell.getStringCellValue();

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

      MessageDTO messageDTO;
      if (messages.containsKey(name)) {
        messageDTO = messages.get(name);
      } else {
        messageDTO = new MessageDTO();
        messageDTO.setName(name);
        messageDTO.setCategory(category);
        messageDTO.setTranslated(true);

        messages.put(name, messageDTO);
      }

      var messageTrlDTO = new MessageTrlDTO();
      messageTrlDTO.setId(UUID.randomUUID());
      messageTrlDTO.setValue(valueCell == null ? "" : valueCell.getStringCellValue());
      messageTrlDTO.setIso3Language(language);
      messageTrlDTO.setParentId(messageDTO.getId());
      messageTrlDTO.setTranslated(true);
      messageDTO.getTranslations().add(messageTrlDTO);
    }

    return messages;
  }

  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  public void postLoad() {
    bootstrapI18n();
  }

  @Transactional
  public synchronized void bootstrapI18n() {
    if (hasBootstrapped) {
      return;
    }
    if (!isBootstrapEnabled) {
      hasBootstrapped = true;
      return;
    }

    var loggerPrefix = getLoggerPrefix("bootstrapI18n");
    try {
      byte[] content = Files.readAllBytes(Path.of(bootstrapFile));

      UUID uploadId = uploadFile(bootstrapFile, ArrayUtils.toObject(content));
      SubmitUploadCommand submitUploadCommand = new SubmitUploadCommand(uploadId, bootstrapFile);

      commandGateway.sendAndWait(submitUploadCommand);

      hasBootstrapped = true;

    } catch (IOException e) {
      error(loggerPrefix, e, "Something wrong happen : {0}", e.getMessage());
    }
  }

  @Override
  public BaseRepository<FileUpload> getRepository() {
    return repository;
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }
}
