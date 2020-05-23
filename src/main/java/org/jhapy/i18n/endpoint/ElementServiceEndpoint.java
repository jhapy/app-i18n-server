package org.jhapy.i18n.endpoint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.service.ElementService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/elementService")
public class ElementServiceEndpoint extends BaseEndpoint {

  private final ElementService elementService;

  public ElementServiceEndpoint(ElementService elementService,
      OrikaBeanMapper mapperFacade) {
    super(mapperFacade);
    this.elementService = elementService;
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/findAnyMatching")
  public ResponseEntity<ServiceResult> findAnyMatching(@RequestBody FindAnyMatchingQuery query) {
    String loggerPrefix = getLoggerPrefix("findAnyMatching");
    try {
      Page<Element> result = elementService
          .findAnyMatching(query.getFilter(), mapperFacade.map(query.getPageable(),
              Pageable.class, getOrikaContext(query)));
      return handleResult(loggerPrefix,
          mapperFacade.map(result, org.jhapy.dto.utils.Page.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/countAnyMatching")
  public ResponseEntity<ServiceResult> countAnyMatching(@RequestBody CountAnyMatchingQuery query) {
    String loggerPrefix = getLoggerPrefix("countAnyMatching");
    try {
      return handleResult(loggerPrefix, elementService
          .countAnyMatching(query.getFilter()));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/getById")
  public ResponseEntity<ServiceResult> getById(@RequestBody GetByIdQuery query) {
    String loggerPrefix = getLoggerPrefix("getById");
    try {
      return handleResult(loggerPrefix, mapperFacade.map(elementService
          .load(query.getId()), Element.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasScope('frontend')")
  @PostMapping(value = "/save")
  public ResponseEntity<ServiceResult> save(
      @RequestBody SaveQuery<org.jhapy.dto.domain.i18n.Element> query) {
    String loggerPrefix = getLoggerPrefix("save");
    try {
      return handleResult(loggerPrefix, mapperFacade.map(elementService
              .save(mapperFacade
                  .map(query.getEntity(), Element.class, getOrikaContext(query))),
          org.jhapy.dto.domain.i18n.Element.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasScope('frontend')")
  @PostMapping(value = "/delete")
  public ResponseEntity<ServiceResult> delete(@RequestBody DeleteByIdQuery query) {
    String loggerPrefix = getLoggerPrefix("delete");
    try {
      elementService
          .delete(query.getId());
      return handleResult(loggerPrefix);
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }
}