package org.jhapy.i18n.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.DatabaseCleanCommand;
import org.jhapy.cqrs.command.ImportUploadCommand;
import org.jhapy.cqrs.command.ValidateUploadCommand;
import org.jhapy.cqrs.event.DatabaseCleanedEvent;
import org.jhapy.cqrs.event.i18n.*;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class UploadProcessorSaga implements HasLogger {
  private transient CommandGateway commandGateway;

  @Autowired
  public void getCommandGateway(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  @StartSaga
  @SagaEventHandler(associationProperty = "uploadId")
  public void handle(UploadSubmittedEvent event) {
    String loggerPrefix = getLoggerPrefix("uploadSubmittedEvent");

    debug(loggerPrefix, "UploadSubmittedEvent is handled for uploadId: {0}", event.getUploadId());

    ValidateUploadCommand validateUploadCommand =
        new ValidateUploadCommand(event.getId(), event.getUploadId());

    commandGateway.send(validateUploadCommand);
  }

  @SagaEventHandler(associationProperty = "uploadId")
  public void handle(FileValidatedEvent event) {
    String loggerPrefix = getLoggerPrefix("fileValidatedEvent");

    debug(loggerPrefix, "FileValidatedEvent is handled for uploadId: {0}", event.getUploadId());

    DatabaseCleanCommand databaseCleanCommand =
        new DatabaseCleanCommand(event.getId(), event.getUploadId());

    commandGateway.send(databaseCleanCommand);
  }

  @SagaEventHandler(associationProperty = "uploadId")
  public void handle(DatabaseCleanedEvent event) {
    String loggerPrefix = getLoggerPrefix("databaseCleanedEvent");

    debug(loggerPrefix, "FileValidatedEvent is handled for uploadId: {0}", event.getUploadId());

    ImportUploadCommand importUploadCommand =
        new ImportUploadCommand(event.getId(), event.getUploadId());

    commandGateway.send(importUploadCommand);
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "uploadId")
  public void handle(FileNotValidatedEvent event) {
    String loggerPrefix = getLoggerPrefix("fileNotValidatedEvent");

    debug(loggerPrefix, "FileNotValidatedEvent is handled for uploadId: {0}", event.getUploadId());
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "uploadId")
  public void handle(FileImportedEvent event) {
    String loggerPrefix = getLoggerPrefix("fileImportedEvent");

    debug(loggerPrefix, "FileImportedEvent is handled for uploadId: {0}", event.getUploadId());
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "uploadId")
  public void handle(FileNotImportedEvent event) {
    String loggerPrefix = getLoggerPrefix("fileNotImportedEvent");

    debug(loggerPrefix, "FileNotValidatedEvent is handled for uploadId: {0}", event.getUploadId());
  }
}
