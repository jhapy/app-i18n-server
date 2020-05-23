package org.jhapy.i18n.listeners;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.SpringApplicationContext;
import org.jhapy.i18n.domain.ActionTrl;
import org.jhapy.i18n.service.ActionTrlService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2/10/20
 */
@Component
public class ActionTrlListener {

  private ActionTrlService actionTrlService;

  @PostUpdate
  @PostPersist
  public void postUpdate(ActionTrl actionTrl) {
    if ( getActionTrlService() != null )
    getActionTrlService().postUpdate(actionTrl);
  }

  protected ActionTrlService getActionTrlService() {
    if (actionTrlService == null) {
      actionTrlService = SpringApplicationContext.getBean(ActionTrlService.class);
    }
    return actionTrlService;
  }

}
