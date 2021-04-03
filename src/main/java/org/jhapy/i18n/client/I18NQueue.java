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

package org.jhapy.i18n.client;

import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.messageQueue.I18NActionTrlUpdate;
import org.jhapy.dto.messageQueue.I18NActionUpdate;
import org.jhapy.dto.messageQueue.I18NElementTrlUpdate;
import org.jhapy.dto.messageQueue.I18NElementUpdate;
import org.jhapy.dto.messageQueue.I18NMessageTrlUpdate;
import org.jhapy.dto.messageQueue.I18NMessageUpdate;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-02
 */
@Component
public class I18NQueue implements HasLogger {

  private final AmqpTemplate amqpTemplate;
private final FanoutExchange elementUpdateFanout;
  private final FanoutExchange elementTrlUpdateFanout;
  private final FanoutExchange actionUpdateFanout;
  private final FanoutExchange actionTrlUpdateFanout;
  private final FanoutExchange messageUpdateFanout;
  private final FanoutExchange messageTrlUpdateFanout;

  public I18NQueue(AmqpTemplate amqpTemplate,
      @Qualifier("elementUpdate") FanoutExchange elementUpdateFanout,
      @Qualifier("elementTrlUpdate") FanoutExchange elementTrlUpdateFanout,
      @Qualifier("actionUpdate") FanoutExchange actionUpdateFanout,
      @Qualifier("actionTrlUpdate") FanoutExchange actionTrlUpdateFanout,
      @Qualifier("messageUpdate") FanoutExchange messageUpdateFanout,
      @Qualifier("messageTrlUpdate") FanoutExchange messageTrlUpdateFanout) {
    this.amqpTemplate = amqpTemplate;
    this.elementUpdateFanout = elementUpdateFanout;
    this.elementTrlUpdateFanout = elementTrlUpdateFanout;
    this.actionUpdateFanout = actionUpdateFanout;
    this.actionTrlUpdateFanout = actionTrlUpdateFanout;
    this.messageUpdateFanout = messageUpdateFanout;
    this.messageTrlUpdateFanout = messageTrlUpdateFanout;
  }

  public void sendElementUpdate(final I18NElementUpdate elementUpdate) {
    String loggerPrefix= getLoggerPrefix("sendElementUpdate", elementUpdate);
    logger().debug(loggerPrefix+"Send Update");
    amqpTemplate.convertAndSend(elementUpdateFanout.getName(), "", elementUpdate);
  }

  public void sendElementTrlUpdate(final I18NElementTrlUpdate elementTrlUpdate) {
    String loggerPrefix= getLoggerPrefix("sendElementTrlUpdate", elementTrlUpdate);
    logger().debug(loggerPrefix+"Send Update");
    amqpTemplate.convertAndSend(elementTrlUpdateFanout.getName(), "", elementTrlUpdate);
  }

  public void sendActionUpdate(final I18NActionUpdate actionUpdate) {
    String loggerPrefix= getLoggerPrefix("sendActionUpdate", actionUpdate);
    logger().debug(loggerPrefix+"Send Update");
    amqpTemplate.convertAndSend(actionUpdateFanout.getName(), "", actionUpdate);
  }

  public void sendActionTrlUpdate(final I18NActionTrlUpdate actionTrlUpdate) {
    String loggerPrefix= getLoggerPrefix("sendActionTrlUpdate", actionTrlUpdate);
    logger().debug(loggerPrefix+"Send Update");
    amqpTemplate.convertAndSend(actionTrlUpdateFanout.getName(), "", actionTrlUpdate);
  }

  public void sendMessageUpdate(final I18NMessageUpdate messageUpdate) {
    String loggerPrefix= getLoggerPrefix("sendMessageUpdate", messageUpdate);
    logger().debug(loggerPrefix+"Send Update");
    amqpTemplate.convertAndSend(messageUpdateFanout.getName(), "", messageUpdate);
  }

  public void sendMessageTrlUpdate(final I18NMessageTrlUpdate messageTrlUpdate) {
    String loggerPrefix= getLoggerPrefix("sendMessageTrlUpdate", messageTrlUpdate);
    logger().debug(loggerPrefix+"Send Update");
    amqpTemplate.convertAndSend(messageTrlUpdateFanout.getName(), "", messageTrlUpdate);
  }
}
