package org.jhapy.i18n.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.jhapy.cqrs.command.*;
import org.jhapy.cqrs.event.DatabaseCleanedEvent;
import org.jhapy.cqrs.event.i18n.*;
import org.jhapy.i18n.converter.FileUploadConverter;
import org.jhapy.i18n.errorHandeling.FileValidationError;
import org.jhapy.i18n.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

@Aggregate
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FileUploadAggregate extends AbstractBaseAggregate {
  @Autowired private transient FileUploadService fileUploadService;

  private UUID uploadId;

  private String filename;

  private Boolean validated = false;

  private Boolean imported = false;

  private String errorMessage;

  public FileUploadAggregate() {}

  @CommandHandler
  public FileUploadAggregate(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          SubmitUploadCommand command) {
    UploadSubmittedEvent event = FileUploadConverter.INSTANCE.toUploadSubmittedEvent(command);
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(ValidateUploadCommand command) {
    try {
      fileUploadService.validate(command.getUploadId());
      FileValidatedEvent fileValidatedEvent =
          new FileValidatedEvent(command.getId(), command.getUploadId());
      AggregateLifecycle.apply(fileValidatedEvent);
    } catch (FileValidationError fileValidationError) {
      FileNotValidatedEvent fileNotValidatedEvent =
          new FileNotValidatedEvent(
              command.getId(), command.getUploadId(), fileValidationError.getMessage());
      AggregateLifecycle.apply(fileNotValidatedEvent);
    }
  }

  @CommandHandler
  public void handle(DatabaseCleanCommand command) {
    fileUploadService.cleanDatabase();
    DatabaseCleanedEvent databaseCleanedEvent =
        new DatabaseCleanedEvent(command.getId(), command.getUploadId());
    AggregateLifecycle.apply(databaseCleanedEvent);
  }

  @CommandHandler
  public void handle(ImportUploadCommand command) {
    try {
      fileUploadService.importFile(command.getUploadId());
      FileImportedEvent fileImportedEvent =
          new FileImportedEvent(command.getId(), command.getUploadId());
      AggregateLifecycle.apply(fileImportedEvent);
    } catch (IOException fileImportError) {
      FileNotImportedEvent fileNotImportedEvent =
          new FileNotImportedEvent(
              command.getId(), command.getUploadId(), fileImportError.getMessage());
      AggregateLifecycle.apply(fileNotImportedEvent);
    }
  }

  @EventSourcingHandler
  public void on(UploadSubmittedEvent event) {
    FileUploadConverter.INSTANCE.updateAggregateFromUploadSubmittedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(FileNotValidatedEvent event) {
    this.validated = false;
    this.errorMessage = event.getErrorMessage();
  }

  @EventSourcingHandler
  public void on(FileValidatedEvent event) {
    this.validated = true;
  }

  @EventSourcingHandler
  public void on(FileImportedEvent event) {
    this.imported = true;
  }

  @EventSourcingHandler
  public void on(FileNotImportedEvent event) {
    this.imported = false;
    this.errorMessage = event.getErrorMessage();
  }
}
