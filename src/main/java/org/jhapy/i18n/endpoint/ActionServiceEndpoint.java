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
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.jhapy.cqrs.query.i18n.GetActionByNameQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByActionIdQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByIso3LanguageQuery;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByNameQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.GetActionTrlQuery;
import org.jhapy.i18n.domain.Action;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@RestController
@RequestMapping("/api/actionService")
public class ActionServiceEndpoint extends BaseRelationaldbV2Endpoint<Action, ActionDTO> {

  public ActionServiceEndpoint(CommandGateway commandGateway, QueryGateway queryGateway) {
    super(commandGateway, queryGateway);
  }

  @PostMapping(value = "/getActionTrls")
  public ResponseEntity<ServiceResult> getActionTrls(@Valid @RequestBody GetActionTrlQuery query) {
    var loggerPrefix = getLoggerPrefix("getActionTrls");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetActionTrlsByActionIdQuery(query.getActionId()),
                ResponseTypes.multipleInstancesOf(ActionTrlDTO.class))
            .join());
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@Valid @RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetActionTrlsByIso3LanguageQuery(query.getIso3Language()),
                ResponseTypes.multipleInstancesOf(ActionTrlDTO.class))
            .join());
  }

  @PostMapping(value = "/getActionTrlByNameAndIso3")
  public ResponseEntity<ServiceResult> getActionTrlByNameAndIso3(
      @Valid @RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getActionTrlByNameAndIso3");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetActionTrlByNameAndIso3LanguageQuery(
                    query.getName(), query.getIso3Language()),
                ActionTrlDTO.class)
            .join());
  }

  @PostMapping(value = "/getByName")
  public ResponseEntity<ServiceResult> getByName(@Valid @RequestBody GetByNameQuery query) {
    var loggerPrefix = getLoggerPrefix("getByName");
    return handleResult(
        loggerPrefix,
        queryGateway.query(new GetActionByNameQuery(query.getName()), ActionDTO.class).join());
  }
}