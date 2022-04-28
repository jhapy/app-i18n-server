package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.query.i18n.GetElementTrlByElementIdAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetElementTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetElementTrlsByElementIdQuery;
import org.jhapy.cqrs.query.i18n.GetElementTrlsByIso3LanguageQuery;
import org.jhapy.i18n.converter.ElementTrlConverter;
import org.jhapy.i18n.repository.ElementRepository;
import org.jhapy.i18n.repository.ElementTrlRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class ElementTrlQueryHandler implements HasLogger {
  private final ElementTrlRepository repository;
  private final ElementRepository elementRepository;
  private final ElementTrlConverter converter;

  @QueryHandler
  public GetElementTrlByElementIdAndIso3LanguageQuery.Response
      getElementTrlByElementIdAndIso3Language(GetElementTrlByElementIdAndIso3LanguageQuery query) {
    Assert.notNull(query.getElementId(), "Element ID is mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var optElementTrl =
        repository.getByParentIdAndIso3Language(query.getElementId(), query.getIso3Language());
    return optElementTrl.map(elementTrl -> new GetElementTrlByElementIdAndIso3LanguageQuery.Response(
            converter.asDTO(elementTrl, null))).orElseGet(() -> new GetElementTrlByElementIdAndIso3LanguageQuery.Response(null));
  }

  @QueryHandler
  public GetElementTrlByNameAndIso3LanguageQuery.Response getByElementTrlByNameAndIso3Language(
      GetElementTrlByNameAndIso3LanguageQuery query) {
    Assert.notNull(query.getName(), "Name mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var loggerPrefix = getLoggerPrefix("getByElementTrlNameAndLanguage");

    var optElement = elementRepository.getByName(query.getName());
    if (optElement.isPresent()) {
      return new GetElementTrlByNameAndIso3LanguageQuery.Response(
          getElementTrlByElementIdAndIso3Language(
                  new GetElementTrlByElementIdAndIso3LanguageQuery(
                      optElement.get().getId(), query.getIso3Language()))
              .getData());
    } else {
      warn(loggerPrefix, "Element '{0}' not found", query.getName());
      return new GetElementTrlByNameAndIso3LanguageQuery.Response(null);
    }
  }

  @QueryHandler
  public GetElementTrlsByIso3LanguageQuery.Response getElementTrlsByIso3Language(
      GetElementTrlsByIso3LanguageQuery query) {
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    return new GetElementTrlsByIso3LanguageQuery.Response(
        converter.asDTOList(repository.findByIso3Language(query.getIso3Language()), null));
  }

  @QueryHandler
  public GetElementTrlsByElementIdQuery.Response getElementTrlsByElementId(
      GetElementTrlsByElementIdQuery query) {
    Assert.notNull(query.getElementId(), "Element ID is mandatory");

    return new GetElementTrlsByElementIdQuery.Response(
        converter.asDTOList(repository.findByParentId(query.getElementId()), null));
  }
}
