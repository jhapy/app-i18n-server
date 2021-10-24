package org.jhapy.i18n.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.jhapy.cqrs.command.ImportUploadCommand;
import org.jhapy.cqrs.command.SubmitUploadCommand;
import org.jhapy.cqrs.command.ValidateUploadCommand;
import org.jhapy.cqrs.event.*;
import org.jhapy.i18n.converter.FileUploadConverter;
import org.jhapy.i18n.errorHandeling.FileValidationError;
import org.jhapy.i18n.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

@Aggregate
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FileUploadAggregate extends AbstractBaseAggregate {
  @Autowired private transient FileUploadService fileUploadService;

  private UUID uploadId;

  private String filename;

  private Boolean isValidated = false;

  private Boolean isImported = false;

  private String errorMessage;

  @CommandHandler
  public FileUploadAggregate(SubmitUploadCommand command) {
    UploadSubmittedEvent event = FileUploadConverter.INSTANCE.toUploadSubmittedEvent(command);
    AggregateLifecycle.apply(event);
  }

  @CommandHandler
  public void handle(ValidateUploadCommand command) {
    try {
      fileUploadService.validate(command.getUploadId());
      FileValidatedEvent fileValidatedEvent = new FileValidatedEvent();
      fileValidatedEvent.setId(command.getId());
      fileValidatedEvent.setUploadId(command.getUploadId());
      AggregateLifecycle.apply(fileValidatedEvent);
    } catch (FileValidationError fileValidationError) {
      FileNotValidatedEvent fileNotValidatedEvent = new FileNotValidatedEvent();
      fileNotValidatedEvent.setId(command.getId());
      fileNotValidatedEvent.setUploadId(command.getUploadId());
      fileNotValidatedEvent.setErrorMessage(fileValidationError.getMessage());
      AggregateLifecycle.apply(fileNotValidatedEvent);
    }
  }

  @CommandHandler
  public void handle(ImportUploadCommand command) {
    try {
      fileUploadService.importFile(command.getUploadId());
      FileImportedEvent fileImportedEvent = new FileImportedEvent();
      fileImportedEvent.setId(command.getId());
      fileImportedEvent.setUploadId(command.getUploadId());
      AggregateLifecycle.apply(fileImportedEvent);
    } catch (IOException fileImportError) {
      FileNotImportedEvent fileNotImportedEvent = new FileNotImportedEvent();
      fileNotImportedEvent.setId(command.getId());
      fileNotImportedEvent.setUploadId(command.getUploadId());
      fileNotImportedEvent.setErrorMessage(fileImportError.getMessage());
      AggregateLifecycle.apply(fileNotImportedEvent);
    }
  }

  @EventSourcingHandler
  public void on(UploadSubmittedEvent event) {
    FileUploadConverter.INSTANCE.updateAggregateFromUploadSubmittedEvent(event, this);
  }

  @EventSourcingHandler
  public void on(FileNotValidatedEvent event) {
    this.isValidated = false;
    this.errorMessage = event.getErrorMessage();
  }

  @EventSourcingHandler
  public void on(FileValidatedEvent event) {
    this.isValidated = true;
  }

  @EventSourcingHandler
  public void on(FileImportedEvent event) {
    this.isImported = true;
  }

  @EventSourcingHandler
  public void on(FileNotImportedEvent event) {
    this.isImported = false;
    this.errorMessage = event.getErrorMessage();
  }
}