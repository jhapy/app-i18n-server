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
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.GetActionTrlQuery;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.GetElementTrlQuery;
import org.jhapy.i18n.converter.ActionConverter;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.jhapy.i18n.converter.ElementConverter;
import org.jhapy.i18n.converter.ElementTrlConverter;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.service.ActionService;
import org.jhapy.i18n.service.CrudRelationalService;
import org.jhapy.i18n.service.ElementService;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/elementService")
public class ElementServiceEndpoint extends BaseRelationaldbV2Endpoint<Element, ElementDTO> {

  private final ElementService elementService;
  private final ElementTrlConverter elementTrlConverter;

  public ElementServiceEndpoint(
      ElementService elementService,
      ElementConverter converter,
      ElementTrlConverter elementTrlConverter) {
    super(converter);
    this.elementService = elementService;
    this.elementTrlConverter = elementTrlConverter;
  }

  @PostMapping(value = "/getElementTrls")
  public ResponseEntity<ServiceResult> getElementTrls(@RequestBody GetElementTrlQuery query) {
    var loggerPrefix = getLoggerPrefix("getElementTrls");

    return handleResult(
        loggerPrefix,
        elementTrlConverter.asDTOList(
            elementService.getElementTrls(query.getElementId()), getContext(query)));
  }

  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@RequestBody FindByIso3Query query) {
    var loggerPrefix = getLoggerPrefix("findByIso3");

    List<ElementTrl> result = elementService.getElementTrlByIso3Language(query.getIso3Language());

    return handleResult(loggerPrefix, elementTrlConverter.asDTOList(result, getContext(query)));
  }

  @PostMapping(value = "/getElementTrlByNameAndIso3")
  public ResponseEntity<ServiceResult> getElementTrlByNameAndIso3(
      @RequestBody GetByNameAndIso3Query query) {
    var loggerPrefix = getLoggerPrefix("getElementTrlByNameAndIso3");

    ElementTrl result =
        elementService.getByElementTrlNameAndLanguage(query.getName(), query.getIso3Language());

    return handleResult(loggerPrefix, elementTrlConverter.asDTO(result, getContext(query)));
  }

  @Override
  protected CrudRelationalService<Element> getService() {
    return elementService;
  }
}