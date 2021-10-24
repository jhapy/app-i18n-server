package org.jhapy.i18n.command.interceptor;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateMessageCommand;
import org.jhapy.cqrs.command.i18n.UpdateMessageCommand;
import org.jhapy.i18n.domain.MessageLookup;
import org.jhapy.i18n.repository.MessageLookupRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

        Optional<MessageLookup> actionLookupEntity =
            lookupRepository.findByMessageIdOrName(
                createMessageCommand.getId(), createMessageCommand.getEntity().getName());

        if (actionLookupEntity.isPresent()) {
          throw new IllegalArgumentException(
              String.format(
                  MESSAGE_EXISTS_PATTERN,
                  createMessageCommand.getEntity().getName(),
                  createMessageCommand.getId()));
        }
      } else if (UpdateMessageCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        UpdateMessageCommand updateMessageCommand = (UpdateMessageCommand) command.getPayload();

        Optional<MessageLookup> messageLookupEntity =
            lookupRepository.findByName(updateMessageCommand.getEntity().getName());

        if (messageLookupEntity.isPresent()
            && !messageLookupEntity.get().getMessageId().equals(updateMessageCommand.getId())) {
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