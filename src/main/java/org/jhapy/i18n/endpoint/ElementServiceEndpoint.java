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
import org.jhapy.cqrs.query.i18n.*;
import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByNameQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.GetElementTrlQuery;
import org.jhapy.i18n.domain.Element;
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
@RequestMapping("/api/elementService")
public class ElementServiceEndpoint extends BaseRelationaldbV2Endpoint<Element, ElementDTO> {

  public ElementServiceEndpoint(CommandGateway commandGateway, QueryGateway queryGateway) {
    super(commandGateway, queryGateway);
  }

  @PostMapping(value = "/getElementTrls")
  public ResponseEntity<ServiceResult> getElementTrls(
      @Valid @RequestBody GetElementTrlQuery query) {
    var loggerPrefix = getLoggerPrefix("getElementTrls");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetElementTrlsByElementIdQuery(query.getElementId()),
                ResponseTypes.instanceOf(GetElementTrlsByElementIdQuery.Response.class))
            .join());
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@Valid @RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetElementTrlsByIso3LanguageQuery(query.getIso3Language()),
                ResponseTypes.instanceOf(GetElementTrlsByIso3LanguageQuery.Response.class))
            .join());
  }

  @PostMapping(value = "/getElementTrlByNameAndIso3")
  public ResponseEntity<ServiceResult> getElementTrlByNameAndIso3(
      @Valid @RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getElementTrlByNameAndIso3");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetElementTrlByNameAndIso3LanguageQuery(
                    query.getName(), query.getIso3Language()),
                ResponseTypes.instanceOf(GetElementTrlByNameAndIso3LanguageQuery.Response.class))
            .join());
  }

  @PostMapping(value = "/getByName")
  public ResponseEntity<ServiceResult> getByName(@Valid @RequestBody GetByNameQuery query) {
    var loggerPrefix = getLoggerPrefix("getByName");
    return handleResult(
        loggerPrefix,
        queryGateway
            .query(
                new GetElementByNameQuery(query.getName()),
                ResponseTypes.instanceOf(GetElementByNameResponse.class))
            .join());
  }
}
