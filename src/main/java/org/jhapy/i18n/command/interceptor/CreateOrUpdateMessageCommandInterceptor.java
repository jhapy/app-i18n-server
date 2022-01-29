package org.jhapy.i18n.command.interceptor;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateMessageCommand;
import org.jhapy.cqrs.command.i18n.UpdateMessageCommand;
import org.jhapy.i18n.repository.MessageLookupRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class CreateOrUpdateMessageCommandInterceptor
    implements MessageDispatchInterceptor<CommandMessage<?>>, HasLogger {
  static final String MESSAGE_EXISTS_PATTERN =
      "Message with name `%s` or element ID `%s` already exists";
  private final MessageLookupRepository lookupRepository;

  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      List<? extends CommandMessage<?>> messages) {
    return (index, command) -> {
      String loggerPrefix = getLoggerPrefix("handle");

      if (CreateMessageCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        CreateMessageCommand createMessageCommand = (CreateMessageCommand) command.getPayload();

        var actionLookupEntity =
            lookupRepository.findByMessageIdOrName(
                createMessageCommand.getId(), createMessageCommand.getEntity().getName());

        if (actionLookupEntity != null) {
          throw new IllegalArgumentException(
              String.format(
                  MESSAGE_EXISTS_PATTERN,
                  createMessageCommand.getEntity().getName(),
                  createMessageCommand.getId()));
        }
      } else if (UpdateMessageCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        UpdateMessageCommand updateMessageCommand = (UpdateMessageCommand) command.getPayload();

        var messageLookupEntity =
            lookupRepository.findByName(updateMessageCommand.getEntity().getName());

        if (messageLookupEntity != null
            && !messageLookupEntity.getMessageId().equals(updateMessageCommand.getId())) {
          throw new IllegalArgumentException(
              String.format(
                  MESSAGE_EXISTS_PATTERN,
                  updateMessageCommand.getEntity().getName(),
                  updateMessageCommand.getId()));
        }
      }
      return command;
    };
  }
}
