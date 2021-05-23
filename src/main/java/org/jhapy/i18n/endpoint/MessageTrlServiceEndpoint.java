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
import java.util.List;
import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.CountByMessageQuery;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.FindByMessageQuery;
import org.jhapy.i18n.converter.I18NConverterV2;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.MessageTrlService;
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
@RequestMapping("/api/messageTrlService")
public class MessageTrlServiceEndpoint extends BaseEndpoint {

  private final MessageTrlService messageTrlService;

  public MessageTrlServiceEndpoint(MessageTrlService messageTrlService,
      I18NConverterV2 converter) {
    super(converter);
    this.messageTrlService = messageTrlService;
  }

  protected I18NConverterV2 getConverter() {
    return (I18NConverterV2) converter;
  }

  @PostMapping(value = "/findByMessage")
  public ResponseEntity<ServiceResult> findByMessage(@RequestBody FindByMessageQuery query) {
    var loggerPrefix = getLoggerPrefix("findByMessage");

    List<MessageTrl> result = messageTrlService.findByMessage(query.getMessageId());

    return handleResult(loggerPrefix, getConverter().convertToDtoMessageTrls(result));
  }

  @PostMapping(value = "/countByMessage")
  public ResponseEntity<ServiceResult> countByMessage(@RequestBody CountByMessageQuery query) {
    var loggerPrefix = getLoggerPrefix("countByMessage");

    return handleResult(loggerPrefix, messageTrlService
        .countByMessage(query.getMessageId()));
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");

    List<MessageTrl> result = messageTrlService
        .getByIso3Language(query.getIso3Language());

    return handleResult(loggerPrefix, getConverter().convertToDtoMessageTrls(result));
  }

  @PostMapping(value = "/getByNameAndIso3")
  public ResponseEntity<ServiceResult> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getByNameAndIso3");

    var result = messageTrlService
        .getByNameAndIso3Language(query.getName(), query.getIso3Language());

    return handleResult(loggerPrefix, getConverter().convertToDto(result));
  }

  @PostMapping(value = "/getById")
  public ResponseEntity<ServiceResult> getById(@RequestBody GetByIdQuery query) {
    var loggerPrefix = getLoggerPrefix("getById");

    return handleResult(loggerPrefix,
        getConverter().convertToDto(messageTrlService.load(query.getId())));
  }

  @Operation(
      security = @SecurityRequirement(name = "openId", scopes = {"ROLE_I18N_WRITE",
          "ROLE_I18N_ADMIN"})
  )
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/save")
  public ResponseEntity<ServiceResult> save(
      @RequestBody SaveQuery<org.jhapy.dto.domain.i18n.MessageTrl> query) {
    var loggerPrefix = getLoggerPrefix("save");

    return handleResult(loggerPrefix, getConverter()
        .convertToDto(messageTrlService.save(getConverter().convertToDomain(query.getEntity()))));
  }

  @Operation(
      security = @SecurityRequirement(name = "openId", scopes = {"ROLE_I18N_WRITE",
          "ROLE_I18N_ADMIN"})
  )
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/delete")
  public ResponseEntity<ServiceResult> delete(@RequestBody DeleteByIdQuery query) {
    var loggerPrefix = getLoggerPrefix("delete");

    messageTrlService
        .delete(query.getId());
    return handleResult(loggerPrefix);
  }
}