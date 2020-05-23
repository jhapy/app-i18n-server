package org.jhapy.i18n.endpoint;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.jhapy.commons.endpoint.BaseEndpoint;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.CountByElementQuery;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.FindByElementQuery;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.service.ElementTrlService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/elementTrlService")
public class ElementTrlServiceEndpoint extends BaseEndpoint {

  private final ElementTrlService elementTrlService;

  public ElementTrlServiceEndpoint(ElementTrlService elementTrlService,
      OrikaBeanMapper mapperFacade) {
    super(mapperFacade);
    this.elementTrlService = elementTrlService;
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_READ')")
  @PostMapping(value = "/findByElement")
  public ResponseEntity<ServiceResult> findByElement(@RequestBody FindByElementQuery query) {
    String loggerPrefix = getLoggerPrefix("findByElement");
    try {
      List<ElementTrl> result = elementTrlService.findByElement(query.getElementId());

      return handleResult(loggerPrefix, mapperFacade.mapAsList(result,
          org.jhapy.dto.domain.i18n.ElementTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_READ')")
  @PostMapping(value = "/countByElement")
  public ResponseEntity<ServiceResult> countByElement(@RequestBody CountByElementQuery query) {
    String loggerPrefix = getLoggerPrefix("countByElement");
    try {
      return handleResult(loggerPrefix, elementTrlService
          .countByElement(query.getElementId()));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_READ')")
  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@RequestBody FindByIso3Query query) {
    String loggerPrefix = getLoggerPrefix("findByIso3");
    try {
      List<ElementTrl> result = elementTrlService
          .getByIso3Language(query.getIso3Language());

      return handleResult(loggerPrefix, mapperFacade
          .mapAsList(result, org.jhapy.dto.domain.i18n.ElementTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_READ')")
  @PostMapping(value = "/getByNameAndIso3")
  public ResponseEntity<ServiceResult> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query) {
    String loggerPrefix = getLoggerPrefix("getByNameAndIso3");
    try {
      ElementTrl result = elementTrlService
          .getByNameAndIso3Language(query.getName(), query.getIso3Language());

      return handleResult(loggerPrefix, mapperFacade
          .map(result, org.jhapy.dto.domain.i18n.ElementTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_READ')")
  @PostMapping(value = "/getById")
  public ResponseEntity<ServiceResult> getById(@RequestBody GetByIdQuery query) {
    String loggerPrefix = getLoggerPrefix("getById");
    try {
      return handleResult(loggerPrefix, mapperFacade.map(elementTrlService
              .load(query.getId()), org.jhapy.dto.domain.i18n.ElementTrl.class,
          getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_WRITE')")
  @PostMapping(value = "/save")
  public ResponseEntity<ServiceResult> save(
      @RequestBody SaveQuery<org.jhapy.dto.domain.i18n.ElementTrl> query) {
    String loggerPrefix = getLoggerPrefix("save");
    try {
      return handleResult(loggerPrefix, mapperFacade.map(elementTrlService
              .save(mapperFacade
                  .map(query.getEntity(), ElementTrl.class, getOrikaContext(query))),
          org.jhapy.dto.domain.i18n.ElementTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.clientHasAnyRole('ROLE_I18N_WRITE')")
  @PostMapping(value = "/delete")
  public ResponseEntity<ServiceResult> delete(@RequestBody DeleteByIdQuery query) {
    String loggerPrefix = getLoggerPrefix("delete");
    try {
      elementTrlService
          .delete(query.getId());
      return handleResult(loggerPrefix);
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }
}