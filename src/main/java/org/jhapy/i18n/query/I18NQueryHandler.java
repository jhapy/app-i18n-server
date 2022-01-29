package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.query.i18n.GetExistingLanguagesQuery;
import org.jhapy.cqrs.query.i18n.GetExistingLanguagesResponse;
import org.jhapy.i18n.repository.ActionTrlRepository;
import org.jhapy.i18n.repository.ElementTrlRepository;
import org.jhapy.i18n.repository.MessageTrlRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class I18NQueryHandler implements HasLogger {
  private final MessageTrlRepository messageTrlRepository;
  private final ElementTrlRepository elementTrlRepository;
  private final ActionTrlRepository actionTrlRepository;

  @SuppressWarnings("unused")
  @QueryHandler
  public GetExistingLanguagesResponse getExistingLanguages(GetExistingLanguagesQuery query) {
    String loggerPrefix = getLoggerPrefix("getExistingLanguages");

    Set<String> detectedLanguages = new HashSet<>();

    detectedLanguages.addAll(messageTrlRepository.getDistinctIso3Language());
    detectedLanguages.addAll(elementTrlRepository.getDistinctIso3Language());
    detectedLanguages.addAll(actionTrlRepository.getDistinctIso3Language());

    debug(loggerPrefix, "Found : {0} languages", detectedLanguages.size());

    return new GetExistingLanguagesResponse(detectedLanguages);
  }
}
