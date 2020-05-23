package org.jhapy.i18n.listeners;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.SpringApplicationContext;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.MessageTrlService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2/10/20
 */
@Component
public class MessageTrlListener {

  private MessageTrlService messageTrlService;

  @PostUpdate
  @PostPersist
  public void postUpdate(MessageTrl messageTrl) {
    if ( getMessageTrlService() != null )
      getMessageTrlService().postUpdate(messageTrl);
  }

  protected MessageTrlService getMessageTrlService() {
    if (messageTrlService == null) {
      messageTrlService = SpringApplicationContext.getBean(MessageTrlService.class);
    }
    return messageTrlService;
  }

}
