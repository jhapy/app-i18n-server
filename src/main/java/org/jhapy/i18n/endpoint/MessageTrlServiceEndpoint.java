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
import org.jhapy.dto.serviceQuery.i18n.messageTrl.CountByMessageQuery;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.FindByMessageQuery;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.MessageTrlService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-05
 */
@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/messageTrlService")
public class MessageTrlServiceEndpoint extends BaseEndpoint {

  private final MessageTrlService messageTrlService;

  public MessageTrlServiceEndpoint(MessageTrlService messageTrlService,
      OrikaBeanMapper mapperFacade) {
    super(mapperFacade);
    this.messageTrlService = messageTrlService;
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/findByMessage")
  public ResponseEntity<ServiceResult> findByMessage(@RequestBody FindByMessageQuery query) {
    String loggerPrefix = getLoggerPrefix("findByMessage");
    try {
      List<MessageTrl> result = messageTrlService.findByMessage(query.getMessageId());

      return handleResult(loggerPrefix, mapperFacade.mapAsList(result,
          org.jhapy.dto.domain.i18n.MessageTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/countByMessage")
  public ResponseEntity<ServiceResult> countByMessage(@RequestBody CountByMessageQuery query) {
    String loggerPrefix = getLoggerPrefix("countByMessage");
    try {
      return handleResult(loggerPrefix, messageTrlService
          .countByMessage(query.getMessageId()));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/findByIso3")
  public ResponseEntity<ServiceResult> findByIso3(@RequestBody FindByIso3Query query) {
    String loggerPrefix = getLoggerPrefix("findByIso3");
    try {
      List<MessageTrl> result = messageTrlService
          .getByIso3Language(query.getIso3Language());

      return handleResult(loggerPrefix, mapperFacade
          .mapAsList(result, org.jhapy.dto.domain.i18n.MessageTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/getByNameAndIso3")
  public ResponseEntity<ServiceResult> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query) {
    String loggerPrefix = getLoggerPrefix("getByNameAndIso3");
    try {
      MessageTrl result = messageTrlService
          .getByNameAndIso3Language(query.getName(), query.getIso3Language());

      return handleResult(loggerPrefix, mapperFacade
          .map(result, org.jhapy.dto.domain.i18n.MessageTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasAnyScope('frontend','mobile','backend')")
  @PostMapping(value = "/getById")
  public ResponseEntity<ServiceResult> getById(@RequestBody GetByIdQuery query) {
    String loggerPrefix = getLoggerPrefix("getById");
    try {
      return handleResult(loggerPrefix, mapperFacade.map(messageTrlService
              .load(query.getId()), org.jhapy.dto.domain.i18n.MessageTrl.class,
          getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasScope('frontend')")
  @PostMapping(value = "/save")
  public ResponseEntity<ServiceResult> save(
      @RequestBody SaveQuery<org.jhapy.dto.domain.i18n.MessageTrl> query) {
    String loggerPrefix = getLoggerPrefix("save");
    try {
      return handleResult(loggerPrefix, mapperFacade.map(messageTrlService
              .save(mapperFacade
                  .map(query.getEntity(), MessageTrl.class, getOrikaContext(query))),
          org.jhapy.dto.domain.i18n.MessageTrl.class, getOrikaContext(query)));
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }

  @PreAuthorize("#oauth2.hasScope('frontend')")
  @PostMapping(value = "/delete")
  public ResponseEntity<ServiceResult> delete(@RequestBody DeleteByIdQuery query) {
    String loggerPrefix = getLoggerPrefix("delete");
    try {
      messageTrlService
          .delete(query.getId());
      return handleResult(loggerPrefix);
    } catch (Throwable t) {
      return handleResult(loggerPrefix, t);
    }
  }
}