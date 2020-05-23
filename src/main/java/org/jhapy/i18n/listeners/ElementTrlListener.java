package org.jhapy.i18n.listeners;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.SpringApplicationContext;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.service.ElementTrlService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2/10/20
 */
@Component
public class ElementTrlListener {

  private ElementTrlService elementTrlService;

  @PostUpdate
  @PostPersist
  public void postUpdate(ElementTrl elementTrl) {
    if ( getElementTrlService() != null )
      getElementTrlService().postUpdate(elementTrl);
  }

  protected ElementTrlService getElementTrlService() {
    if (elementTrlService == null) {
      elementTrlService = SpringApplicationContext.getBean(ElementTrlService.class);
    }
    return elementTrlService;
  }

}
