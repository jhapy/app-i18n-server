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

import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.GetActionTrlQuery;
import org.jhapy.i18n.converter.ActionConverter;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.service.ActionService;
import org.jhapy.i18n.service.CrudRelationalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@RestController
@RequestMapping("/api/actionService")
public class ActionServiceEndpoint extends BaseRelationaldbV2Endpoint<Action, ActionDTO> {

  private final ActionService actionService;
  private final ActionTrlConverter actionTrlConverter;

  public ActionServiceEndpoint(
      ActionService actionService,
      ActionConverter converter,
      ActionTrlConverter actionTrlConverter) {
    super(converter);
    this.actionService = actionService;
    this.actionTrlConverter = actionTrlConverter;
  }

  @PostMapping(value = "/getActionTrls")
  public ResponseEntity<ServiceResult> getActionTrls(@RequestBody GetActionTrlQuery query) {
    var loggerPrefix = getLoggerPrefix("getActionTrls");

    return handleResult(
        loggerPrefix,
        actionTrlConverter.asDTOList(
            actionService.getActionTrls(query.getActionId()), getContext(query)));
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");

    List<ActionTrl> result = actionService.getActionTrlByIso3Language(query.getIso3Language());

    return handleResult(loggerPrefix, actionTrlConverter.asDTOList(result, getContext(query)));
  }

  @PostMapping(value = "/getActionTrlByNameAndIso3")
  public ResponseEntity<ServiceResult> getActionTrlByNameAndIso3(
      @RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getActionTrlByNameAndIso3");

    ActionTrl result =
        actionService.getByActionTrlNameAndLanguage(query.getName(), query.getIso3Language());

    return handleResult(loggerPrefix, actionTrlConverter.asDTO(result, getContext(query)));
  }

  @Override
  protected CrudRelationalService<Action> getService() {
    return actionService;
  }
}