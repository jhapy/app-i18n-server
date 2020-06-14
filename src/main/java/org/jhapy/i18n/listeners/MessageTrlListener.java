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

package org.jhapy.i18n.listeners;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import org.jhapy.commons.utils.SpringApplicationContext;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.service.MessageTrlService;
import org.springframework.stereotype.Component;

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
    if (getMessageTrlService() != null) {
      getMessageTrlService().postUpdate(messageTrl);
    }
  }

  protected MessageTrlService getMessageTrlService() {
    if (messageTrlService == null) {
      messageTrlService = SpringApplicationContext.getBean(MessageTrlService.class);
    }
    return messageTrlService;
  }

}
