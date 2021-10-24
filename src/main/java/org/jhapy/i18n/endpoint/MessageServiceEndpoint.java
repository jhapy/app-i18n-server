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
import org.jhapy.cqrs.query.i18n.GetMessageByNameQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlsByIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlsByMessageIdQuery;
import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.dto.domain.i18n.MessageTrlDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByNameQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.GetMessageTrlQuery;
import org.jhapy.i18n.domain.Message;
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
@RequestMapping("/api/messageService")
public class MessageServiceEndpoint extends BaseRelationaldbV2Endpoint<Message, MessageDTO> {

  public MessageServiceEndpoint(CommandGateway commandGateway, QueryGateway queryGateway) {
    super(commandGateway, queryGateway);
  }

  @PostMapping(value = "/getMessageTrls")
  public ResponseEntity<ServiceResult> getMessageTrls(
      @Valid @RequestBody GetMessageTrlQuery query) {
    var loggerPrefix = getLoggerPrefix("getMessageTrls");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetMessageTrlsByMessageIdQuery(query.getMessageId()),
                ResponseTypes.multipleInstancesOf(MessageTrlDTO.class))
            .join());
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@Valid @RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetMessageTrlsByIso3LanguageQuery(query.getIso3Language()),
                ResponseTypes.multipleInstancesOf(MessageTrlDTO.class))
            .join());
  }

  @PostMapping(value = "/getMessageTrlByNameAndIso3")
  public ResponseEntity<ServiceResult> getMessageTrlByNameAndIso3(
      @Valid @RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getMessageTrlByNameAndIso3");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetMessageTrlByNameAndIso3LanguageQuery(
                    query.getName(), query.getIso3Language()),
                MessageTrlDTO.class)
            .join());
  }

  @PostMapping(value = "/getByName")
  public ResponseEntity<ServiceResult> getByName(@Valid @RequestBody GetByNameQuery query) {
    var loggerPrefix = getLoggerPrefix("getByName");
    return handleResult(
        loggerPrefix,
        queryGateway.query(new GetMessageByNameQuery(query.getName()), MessageDTO.class).join());
  }
}