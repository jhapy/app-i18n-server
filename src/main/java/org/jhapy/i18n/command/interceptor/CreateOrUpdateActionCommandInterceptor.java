package org.jhapy.i18n.command.interceptor;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateActionCommand;
import org.jhapy.cqrs.command.i18n.UpdateActionCommand;
import org.jhapy.i18n.repository.ActionLookupRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class CreateOrUpdateActionCommandInterceptor
    implements MessageDispatchInterceptor<CommandMessage<?>>, HasLogger {
  static final String ACTION_EXISTS_PATTERN =
      "Action with name `%s` or element ID `%s` already exists";
  private final ActionLookupRepository lookupRepository;

  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      List<? extends CommandMessage<?>> messages) {
    return (index, command) -> {
      String loggerPrefix = getLoggerPrefix("handle");

      if (CreateActionCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        CreateActionCommand createActionCommand = (CreateActionCommand) command.getPayload();

        var actionLookupEntity =
            lookupRepository.findByActionIdOrName(
                createActionCommand.getId(), createActionCommand.getEntity().getName());

        if (actionLookupEntity.isPresent()) {
          throw new IllegalArgumentException(
              String.format(
                  ACTION_EXISTS_PATTERN,
                  createActionCommand.getEntity().getName(),
                  createActionCommand.getId()));
        }
      } else if (UpdateActionCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        UpdateActionCommand updateActionCommand = (UpdateActionCommand) command.getPayload();

        var elementLookupEntity =
            lookupRepository.findByName(updateActionCommand.getEntity().getName());

        if (elementLookupEntity != null
            && !elementLookupEntity.get().getActionId().equals(updateActionCommand.getId())) {
          throw new IllegalArgumentException(
              String.format(
                  ACTION_EXISTS_PATTERN,
                  updateActionCommand.getEntity().getName(),
                  updateActionCommand.getId()));
        }
      }
      return command;
    };
  }
}
