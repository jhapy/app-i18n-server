package org.jhapy.i18n.command.interceptor;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateElementCommand;
import org.jhapy.cqrs.command.i18n.UpdateElementCommand;
import org.jhapy.i18n.domain.ElementLookup;
import org.jhapy.i18n.repository.ElementLookupRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class CreateOrUpdateElementCommandInterceptor
    implements MessageDispatchInterceptor<CommandMessage<?>>, HasLogger {
  static final String ELEMENT_EXISTS_PATTERN =
      "Element with name `%s` or element ID `%s` already exists";
  private final ElementLookupRepository lookupRepository;

  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      List<? extends CommandMessage<?>> messages) {
    return (index, command) -> {
      String loggerPrefix = getLoggerPrefix("handle");

      if (CreateElementCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        CreateElementCommand createElementCommand = (CreateElementCommand) command.getPayload();

        Optional<ElementLookup> elementLookupEntity =
            lookupRepository.findByElementIdOrName(
                createElementCommand.getId(), createElementCommand.getEntity().getName());

        if (elementLookupEntity.isPresent()) {
          throw new IllegalArgumentException(
              String.format(
                  ELEMENT_EXISTS_PATTERN,
                  createElementCommand.getEntity().getName(),
                  createElementCommand.getId()));
        }
      } else if (UpdateElementCommand.class.equals(command.getPayloadType())) {
        debug(loggerPrefix, "Intercepted command type: {0}", command.getPayloadType());
        UpdateElementCommand updateElementCommand = (UpdateElementCommand) command.getPayload();

        Optional<ElementLookup> elementLookupEntity =
            lookupRepository.findByName(updateElementCommand.getEntity().getName());

        if (elementLookupEntity.isPresent()
            && !elementLookupEntity.get().getElementId().equals(updateElementCommand.getId())) {
          throw new IllegalArgumentException(
              String.format(
                  ELEMENT_EXISTS_PATTERN,
                  updateElementCommand.getEntity().getName(),
                  updateElementCommand.getId()));
        }
      }
      return command;
    };
  }
}
