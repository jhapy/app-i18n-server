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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.i18n.converter.I18NConverterV2;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@RestController
@RequestMapping("/api/messageService")
public class MessageServiceEndpoint extends BaseEndpoint {

  private final MessageService messageService;

  public MessageServiceEndpoint(MessageService messageService,
      I18NConverterV2 converter) {
    super(converter);
    this.messageService = messageService;
  }

  protected I18NConverterV2 getConverter() {
    return (I18NConverterV2) converter;
  }

  @PostMapping(value = "/findAnyMatching")
  public ResponseEntity<ServiceResult> findAnyMatching(@RequestBody FindAnyMatchingQuery query) {
    var loggerPrefix = getLoggerPrefix("findAnyMatching");

    logger().debug(loggerPrefix + "Query = " + query);

    Page<Message> result = messageService
        .findAnyMatching(query.getQueryUsername(), query.getFilter(), query.getShowInactive(),
            converter.convert(query.getPageable()));

    logger().debug(loggerPrefix + "Result size = " + result.getContent().size());
    return handleResult(loggerPrefix,
        toDtoPage(result, getConverter().convertToDtoMessages(result.getContent())));
  }

  @PostMapping(value = "/countAnyMatching")
  public ResponseEntity<ServiceResult> countAnyMatching(@RequestBody CountAnyMatchingQuery query) {
    var loggerPrefix = getLoggerPrefix("countAnyMatching");

    return handleResult(loggerPrefix, messageService
        .countAnyMatching(query.getQueryUsername(), query.getFilter(), query.getShowInactive()));
  }

  @PostMapping(value = "/getById")
  public ResponseEntity<ServiceResult> getById(@RequestBody GetByIdQuery query) {
    var loggerPrefix = getLoggerPrefix("getById");

    return handleResult(loggerPrefix,
        getConverter().convertToDto(messageService.load(query.getId())));
  }

  @Operation(
      security = @SecurityRequirement(name = "openId", scopes = {"ROLE_I18N_WRITE",
          "ROLE_I18N_ADMIN"})
  )
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/save")
  public ResponseEntity<ServiceResult> save(
      @RequestBody SaveQuery<org.jhapy.dto.domain.i18n.Message> query) {
    var loggerPrefix = getLoggerPrefix("save");

    org.jhapy.i18n.domain.Message converted = getConverter().convertToDomain(query.getEntity());
    if (query.getEntity().getTranslations() != null) {
      converted.setTranslations(
          getConverter().convertToDomainMessageTrls(query.getEntity().getTranslations()));
    }
    return handleResult(loggerPrefix, getConverter().convertToDto(messageService.save(converted)));
  }

  @Operation(
      security = @SecurityRequirement(name = "openId", scopes = {"ROLE_I18N_WRITE",
          "ROLE_I18N_ADMIN"})
  )
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/delete")
  public ResponseEntity<ServiceResult> delete(@RequestBody DeleteByIdQuery query) {
    var loggerPrefix = getLoggerPrefix("delete");

    messageService
        .delete(query.getId());
    return handleResult(loggerPrefix);
  }
}