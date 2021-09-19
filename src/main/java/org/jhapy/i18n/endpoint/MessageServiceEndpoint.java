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

import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.GetMessageTrlQuery;
import org.jhapy.i18n.converter.MessageConverter;
import org.jhapy.i18n.converter.MessageTrlConverter;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.CrudRelationalService;
import org.jhapy.i18n.service.MessageService;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/messageService")
public class MessageServiceEndpoint extends BaseRelationaldbV2Endpoint<Message, MessageDTO> {

  private final MessageService messageService;
  private final MessageTrlConverter messageTrlConverter;

  public MessageServiceEndpoint(
      MessageService messageService,
      MessageConverter converter,
      MessageTrlConverter messageTrlConverter) {
    super(converter);
    this.messageService = messageService;
    this.messageTrlConverter = messageTrlConverter;
  }

  @PostMapping(value = "/getMessageTrls")
  public ResponseEntity<ServiceResult> getMessageTrls(@RequestBody GetMessageTrlQuery query) {
    var loggerPrefix = getLoggerPrefix("getMessageTrls");

    return handleResult(
        loggerPrefix,
        messageTrlConverter.asDTOList(
            messageService.getMessageTrls(query.getMessageId()), getContext(query)));
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");

    List<MessageTrl> result = messageService.getMessageTrlByIso3Language(query.getIso3Language());

    return handleResult(loggerPrefix, messageTrlConverter.asDTOList(result, getContext(query)));
  }

  @PostMapping(value = "/getMessageTrlByNameAndIso3")
  public ResponseEntity<ServiceResult> getMessageTrlByNameAndIso3(
      @RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getMessageTrlByNameAndIso3");

    MessageTrl result =
        messageService.getByMessageTrlNameAndLanguage(query.getName(), query.getIso3Language());

    return handleResult(loggerPrefix, messageTrlConverter.asDTO(result, getContext(query)));
  }

  @Override
  protected CrudRelationalService<Message> getService() {
    return messageService;
  }
}