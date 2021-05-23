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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.dto.domain.i18n.Action;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.i18n.converter.I18NConverterV2;
import org.jhapy.i18n.service.ActionService;
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
@RequestMapping("/api/actionService")
public class ActionServiceEndpoint extends BaseEndpoint {

  private final ActionService actionService;

  public ActionServiceEndpoint(ActionService actionService,
      I18NConverterV2 converter) {
    super(converter);
    this.actionService = actionService;
  }

  protected I18NConverterV2 getConverter() {
    return (I18NConverterV2) converter;
  }

  @PostMapping(value = "/findAnyMatching")
  public ResponseEntity<ServiceResult> findAnyMatching(
      @Parameter(name = "Query parameter", required = true) @RequestBody FindAnyMatchingQuery query) {
    var loggerPrefix = getLoggerPrefix("findAnyMatching");
    Page<org.jhapy.i18n.domain.Action> result = actionService
        .findAnyMatching(query.getQueryUsername(), query.getFilter(), query.getShowInactive(),
            converter.convert(query.getPageable()));
    return handleResult(loggerPrefix,
        toDtoPage(result, getConverter().convertToDtoActions(result.getContent())));
  }

  @PostMapping(value = "/countAnyMatching")
  public ResponseEntity<ServiceResult> countAnyMatching(@RequestBody CountAnyMatchingQuery query) {
    var loggerPrefix = getLoggerPrefix("countAnyMatching");
    return handleResult(loggerPrefix, actionService
        .countAnyMatching(query.getQueryUsername(), query.getFilter(), query.getShowInactive()));
  }

  @PostMapping(value = "/getById")
  public ResponseEntity<ServiceResult> getById(@RequestBody GetByIdQuery query) {
    var loggerPrefix = getLoggerPrefix("getById");
    return handleResult(loggerPrefix,
        getConverter().convertToDto(actionService.load(query.getId())));
  }

  @Operation(
      security = @SecurityRequirement(name = "openId", scopes = {"ROLE_I18N_WRITE",
          "ROLE_I18N_ADMIN"})
  )
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/save")
  public ResponseEntity<ServiceResult> save(
      @RequestBody SaveQuery<Action> query) {
    var loggerPrefix = getLoggerPrefix("save");
    org.jhapy.i18n.domain.Action converted = getConverter().convertToDomain(query.getEntity());
    if (query.getEntity().getTranslations() != null) {
      converted.setTranslations(
          getConverter().convertToDomainActionTrls(query.getEntity().getTranslations()));
    }
    return handleResult(loggerPrefix, getConverter().convertToDto(actionService.save(converted)));
  }

  @Operation(
      security = @SecurityRequirement(name = "openId", scopes = {"ROLE_I18N_WRITE",
          "ROLE_I18N_ADMIN"})
  )
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/delete")
  public ResponseEntity<ServiceResult> delete(@RequestBody DeleteByIdQuery query) {
    var loggerPrefix = getLoggerPrefix("delete");
    actionService
        .delete(query.getId());
    return handleResult(loggerPrefix);
  }
}