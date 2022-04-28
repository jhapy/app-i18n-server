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

package org.jhapy.i18n.endpoint;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.cqrs.command.SubmitUploadCommand;
import org.jhapy.cqrs.query.i18n.GetExistingLanguagesQuery;
import org.jhapy.cqrs.query.i18n.GetExistingLanguagesResponse;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.jhapy.dto.serviceQuery.i18n.QueryFileUploadStatusQuery;
import org.jhapy.dto.serviceQuery.i18n.ResetEventsQuery;
import org.jhapy.i18n.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@RestController
@RequestMapping("/api/i18NService")
public class I18NServiceEndpoint extends BaseEndpoint {

  private final FileUploadService fileUploadService;
  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;
  private final EventProcessingConfiguration eventProcessingConfiguration;

  public I18NServiceEndpoint(
      FileUploadService fileUploadService,
      CommandGateway commandGateway,
      QueryGateway queryGateway,
      EventProcessingConfiguration eventProcessingConfiguration) {
    super(null);
    this.fileUploadService = fileUploadService;
    this.commandGateway = commandGateway;
    this.queryGateway = queryGateway;
    this.eventProcessingConfiguration = eventProcessingConfiguration;
  }

  @PostMapping(value = "/getExistingLanguages")
  public ResponseEntity<ServiceResult> getExistingLanguages(
      @RequestBody GetExistingLanguagesQuery query) {
    var loggerPrefix = getLoggerPrefix("getExistingLanguages");

    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetExistingLanguagesQuery(),
                ResponseTypes.instanceOf(GetExistingLanguagesResponse.class))
            .join());
  }

  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/getI18NFile")
  public ResponseEntity<ServiceResult> getI18NFile(@RequestBody BaseRemoteQuery query) {
    var loggerPrefix = getLoggerPrefix("getI18NFile");


    return  handleResult(loggerPrefix, fileUploadService.getI18NFile());
  }

  // @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/importI18NFile")
  public ResponseEntity<ServiceResult> importI18NFile(@RequestBody ImportI18NFileQuery query) {
    var loggerPrefix = getLoggerPrefix("importI18NFile");

    UUID uploadId = fileUploadService.uploadFile(query.getFilename(), query.getFileContent());
    SubmitUploadCommand submitUploadCommand =
        new SubmitUploadCommand(uploadId, query.getFilename());

    UUID result = commandGateway.sendAndWait(submitUploadCommand);

    if (result == null) {
      return handleResult(loggerPrefix);
    } else {
      return handleResult(loggerPrefix, uploadId);
    }
  }

  @PostMapping(value = "/queryUploadStatus")
  public ResponseEntity<ServiceResult> queryUploadStatus(
      @RequestBody QueryFileUploadStatusQuery query) {
    var loggerPrefix = getLoggerPrefix("queryUploadStatus");
    return handleResult(loggerPrefix, fileUploadService.getFileUploadStatus(query.getUploadId()));
  }

  @PostMapping("/resetEvents")
  public ResponseEntity<ServiceResult> resetEvents(@RequestBody ResetEventsQuery query) {
    var loggerPrefix = getLoggerPrefix("resetEvents");

    Optional<TrackingEventProcessor> eventProcessorOptional =
        eventProcessingConfiguration.eventProcessor(
            query.getEventProcessorName(), TrackingEventProcessor.class);
    if (eventProcessorOptional.isPresent()) {

      TrackingEventProcessor eventProcessor = eventProcessorOptional.get();
      eventProcessor.shutDown();
      eventProcessor.resetTokens();
      eventProcessor.start();

      var result = new ServiceResult<>();
      result.setIsSuccess(true);
      result.setData(
          String.format(
              "The event processor with the name `%s` has been reset",
              query.getEventProcessorName()));
      return handleResult(loggerPrefix, result);
    } else {
      return handleResult(
          loggerPrefix,
          String.format(
              "The event processor with the name `%s` is not a tracking event processor",
              query.getEventProcessorName()));
    }
  }
}
