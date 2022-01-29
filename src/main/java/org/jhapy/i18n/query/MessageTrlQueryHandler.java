package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.query.i18n.GetMessageTrlByMessageIdAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlsByIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetMessageTrlsByMessageIdQuery;
import org.jhapy.i18n.converter.MessageTrlConverter;
import org.jhapy.i18n.repository.MessageRepository;
import org.jhapy.i18n.repository.MessageTrlRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class MessageTrlQueryHandler implements HasLogger {
  private final MessageTrlRepository repository;
  private final MessageRepository messageRepository;
  private final MessageTrlConverter converter;

  @QueryHandler
  public GetMessageTrlByMessageIdAndIso3LanguageQuery.Response
      getMessageTrlByMessageIdAndIso3Language(GetMessageTrlByMessageIdAndIso3LanguageQuery query) {
    Assert.notNull(query.getMessageId(), "Message ID is mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var messageTrl =
        repository.getByParentIdAndIso3Language(query.getMessageId(), query.getIso3Language());
    if (messageTrl != null)
      return new GetMessageTrlByMessageIdAndIso3LanguageQuery.Response(
          converter.asDTO(messageTrl, null));
    else {
      return new GetMessageTrlByMessageIdAndIso3LanguageQuery.Response(null);
    }
  }

  @QueryHandler
  public GetMessageTrlByNameAndIso3LanguageQuery.Response getByMessageTrlByNameAndIso3Language(
      GetMessageTrlByNameAndIso3LanguageQuery query) {
    Assert.notNull(query.getName(), "Name mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var loggerPrefix = getLoggerPrefix("getByMessageTrlNameAndLanguage");

    var message = messageRepository.getByName(query.getName());
    if (message != null) {
      return new GetMessageTrlByNameAndIso3LanguageQuery.Response(
          getMessageTrlByMessageIdAndIso3Language(
                  new GetMessageTrlByMessageIdAndIso3LanguageQuery(
                      message.getId(), query.getIso3Language()))
              .getData());
    } else {
      warn(loggerPrefix, "Message '{0}' not found", query.getName());
      return new GetMessageTrlByNameAndIso3LanguageQuery.Response(null);
    }
  }

  @QueryHandler
  public GetMessageTrlsByIso3LanguageQuery.Response getMessageTrlsByIso3Language(
      GetMessageTrlsByIso3LanguageQuery query) {
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    return new GetMessageTrlsByIso3LanguageQuery.Response(
        converter.asDTOList(repository.findByIso3Language(query.getIso3Language()), null));
  }

  @QueryHandler
  public GetMessageTrlsByMessageIdQuery.Response getMessageTrlsByMessageId(
      GetMessageTrlsByMessageIdQuery query) {
    Assert.notNull(query.getMessageId(), "Message ID is mandatory");

    return new GetMessageTrlsByMessageIdQuery.Response(
        converter.asDTOList(repository.findByParentId(query.getMessageId()), null));
  }
}
