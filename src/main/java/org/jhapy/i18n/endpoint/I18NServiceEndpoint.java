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
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.jhapy.i18n.service.I18nService;
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
@RequestMapping("/api/i18NService")
public class I18NServiceEndpoint extends BaseEndpoint {

  private final I18nService i18nService;

  public I18NServiceEndpoint(I18nService i18nService) {
    super(null);
    this.i18nService = i18nService;
  }

  @PostMapping(value = "/getExistingLanguages")
  public ResponseEntity<ServiceResult> getExistingLanguages(@RequestBody BaseRemoteQuery query) {
    var loggerPrefix = getLoggerPrefix("getExistingLanguages");

    return handleResult(loggerPrefix, i18nService.getExistingLanguages());
  }

  @Operation(
      security =
          @SecurityRequirement(
              name = "openId",
              scopes = {"ROLE_I18N_WRITE", "ROLE_I18N_ADMIN"}))
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/getI18NFile")
  public ResponseEntity<ServiceResult> getI18NFile(@RequestBody BaseRemoteQuery query) {
    var loggerPrefix = getLoggerPrefix("getI18NFile");

    return handleResult(loggerPrefix, i18nService.getI18NFile());
  }

  @Operation(
      security =
          @SecurityRequirement(
              name = "openId",
              scopes = {"ROLE_I18N_WRITE", "ROLE_I18N_ADMIN"}))
  @PreAuthorize("hasAnyAuthority('ROLE_I18N_ADMIN', 'ROLE_I18N_WRITE')")
  @PostMapping(value = "/importI18NFile")
  public ResponseEntity<ServiceResult> importI18NFile(@RequestBody ImportI18NFileQuery query) {
    var loggerPrefix = getLoggerPrefix("importI18NFile");

    String result = i18nService.importI18NFile(query.getFileContent());
    if (result == null) {
      return handleResult(loggerPrefix);
    } else {
      return handleResult(loggerPrefix, result);
    }
  }
}