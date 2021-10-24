package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateMessageCommand;
import org.jhapy.cqrs.command.i18n.CreateMessageTrlCommand;
import org.jhapy.cqrs.query.i18n.GetMessageTrlByMessageIdAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlsByIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlsByMessageIdQuery;
import org.jhapy.dto.domain.i18n.MessageDTO;
import org.jhapy.dto.domain.i18n.MessageTrlDTO;
import org.jhapy.i18n.converter.MessageTrlConverter;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.repository.MessageRepository;
import org.jhapy.i18n.repository.MessageTrlRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MessageTrlQueryHandler implements HasLogger {
  private final MessageTrlRepository repository;
  private final MessageRepository messageRepository;
  private final MessageTrlConverter converter;
  private final CommandGateway commandGateway;

  @QueryHandler
  public MessageTrlDTO getMessageTrlByMessageIdAndIso3Language(
      GetMessageTrlByMessageIdAndIso3LanguageQuery query) {
    Assert.notNull(query.getMessageId(), "Message ID is mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var optMessageTrl =
        repository.getByParentIdAndIso3Language(query.getMessageId(), query.getIso3Language());
    if (optMessageTrl.isPresent()) return converter.asDTO(optMessageTrl.get(), null);
    else {
      var optDefaultMessageTrl = repository.getByParentIdAndIsDefaultIsTrue(query.getMessageId());

      CreateMessageTrlCommand command = new CreateMessageTrlCommand();

      MessageTrlDTO newMessageTrlDTO = new MessageTrlDTO();
      newMessageTrlDTO.setParentId(query.getMessageId());
      newMessageTrlDTO.setIso3Language(query.getIso3Language());
      newMessageTrlDTO.setIsTranslated(false);

      if (optDefaultMessageTrl.isPresent()) {
        newMessageTrlDTO.setValue(optDefaultMessageTrl.get().getValue());
        newMessageTrlDTO.setIsDefault(true);
      } else {
        var message = messageRepository.getById(query.getMessageId());
        newMessageTrlDTO.setValue(message.getName());
        newMessageTrlDTO.setIsDefault(false);
      }
      command.setEntity(newMessageTrlDTO);
      UUID newMessageTrlId = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);

      return converter.asDTO(repository.getById(newMessageTrlId), null);
    }
  }

  @QueryHandler
  public MessageTrlDTO getByMessageTrlByNameAndIso3Language(
      GetMessageTrlByNameAndIso3LanguageQuery query) {
    Assert.notNull(query.getName(), "Name mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var loggerPrefix = getLoggerPrefix("getByMessageTrlNameAndLanguage");

    Optional<Message> optMessage = messageRepository.getByName(query.getName());
    Message message;
    if (optMessage.isEmpty()) {
      warn(loggerPrefix, "Message '{0}' not found, create a new one", query.getName());
      MessageDTO messageDTO = new MessageDTO();
      messageDTO.setName(query.getName());
      messageDTO.setIsTranslated(false);

      MessageTrlDTO newMessageTrlDTO = new MessageTrlDTO();
      newMessageTrlDTO.setIso3Language(query.getIso3Language());
      newMessageTrlDTO.setIsDefault(true);
      newMessageTrlDTO.setIsTranslated(false);
      newMessageTrlDTO.setValue(query.getName());

      messageDTO.getTranslations().add(newMessageTrlDTO);

      CreateMessageCommand command = new CreateMessageCommand();
      command.setEntity(messageDTO);
      UUID newMessageId = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);
      message = messageRepository.getById(newMessageId);
    } else {
      message = optMessage.get();
    }
    return getMessageTrlByMessageIdAndIso3Language(
        new GetMessageTrlByMessageIdAndIso3LanguageQuery(message.getId(), query.getIso3Language()));
  }

  @QueryHandler
  public List<MessageTrlDTO> getMessageTrlsByIso3Language(GetMessageTrlsByIso3LanguageQuery query) {
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    return converter.asDTOList(repository.findByIso3Language(query.getIso3Language()), null);
  }

  @QueryHandler
  public List<MessageTrlDTO> getMessageTrlsByMessageId(GetMessageTrlsByMessageIdQuery query) {
    Assert.notNull(query.getMessageId(), "Message ID is mandatory");

    return converter.asDTOList(repository.findByParentId(query.getMessageId()), null);
  }
}