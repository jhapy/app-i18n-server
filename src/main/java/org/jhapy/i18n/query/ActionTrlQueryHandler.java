package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.query.i18n.GetActionTrlByActionIdAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByActionIdQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByIso3LanguageQuery;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.jhapy.i18n.repository.ActionRepository;
import org.jhapy.i18n.repository.ActionTrlRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class ActionTrlQueryHandler implements HasLogger {
  private final ActionTrlRepository repository;
  private final ActionRepository actionRepository;
  private final ActionTrlConverter converter;

  @QueryHandler
  public GetActionTrlByActionIdAndIso3LanguageQuery.Response getActionTrlByActionIdAndIso3Language(
      GetActionTrlByActionIdAndIso3LanguageQuery query) {
    Assert.notNull(query.getActionId(), "Action ID is mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var optActionTrl =
        repository.getByParentIdAndIso3Language(query.getActionId(), query.getIso3Language());
    return optActionTrl.map(actionTrl -> new GetActionTrlByActionIdAndIso3LanguageQuery.Response(
            converter.asDTO(actionTrl, null))).orElseGet(() -> new GetActionTrlByActionIdAndIso3LanguageQuery.Response(null));
  }

  @QueryHandler
  public GetActionTrlByNameAndIso3LanguageQuery.Response getByActionTrlByNameAndIso3Language(
      GetActionTrlByNameAndIso3LanguageQuery query) {
    Assert.notNull(query.getName(), "Name mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var loggerPrefix = getLoggerPrefix("getByActionTrlNameAndLanguage");

    var optAction = actionRepository.getByName(query.getName());
    if (optAction.isPresent()) {
      return new GetActionTrlByNameAndIso3LanguageQuery.Response(
          getActionTrlByActionIdAndIso3Language(
                  new GetActionTrlByActionIdAndIso3LanguageQuery(
                      optAction.get().getId(), query.getIso3Language()))
              .getData());
    } else {
      warn(loggerPrefix, "Action '{0}' not found", query.getName());
      return new GetActionTrlByNameAndIso3LanguageQuery.Response(null);
    }
  }

  @QueryHandler
  public GetActionTrlsByIso3LanguageQuery.Response getActionTrlsByIso3Language(
      GetActionTrlsByIso3LanguageQuery query) {
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    return new GetActionTrlsByIso3LanguageQuery.Response(
        converter.asDTOList(repository.findByIso3Language(query.getIso3Language()), null));
  }

  @QueryHandler
  public GetActionTrlsByActionIdQuery.Response getActionTrlsByActionId(
      GetActionTrlsByActionIdQuery query) {
    Assert.notNull(query.getActionId(), "Action ID is mandatory");

    return new GetActionTrlsByActionIdQuery.Response(
        converter.asDTOList(repository.findByParentId(query.getActionId()), null));
  }
}
