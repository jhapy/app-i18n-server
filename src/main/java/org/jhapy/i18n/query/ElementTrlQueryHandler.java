package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateElementCommand;
import org.jhapy.cqrs.command.i18n.CreateElementTrlCommand;
import org.jhapy.cqrs.query.i18n.GetElementTrlByElementIdAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetElementTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetElementTrlsByElementIdQuery;
import org.jhapy.cqrs.query.i18n.GetElementTrlsByIso3LanguageQuery;
import org.jhapy.dto.domain.i18n.ElementDTO;
import org.jhapy.dto.domain.i18n.ElementTrlDTO;
import org.jhapy.i18n.converter.ElementTrlConverter;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.repository.ElementRepository;
import org.jhapy.i18n.repository.ElementTrlRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ElementTrlQueryHandler implements HasLogger {
  private final ElementTrlRepository repository;
  private final ElementRepository elementRepository;
  private final ElementTrlConverter converter;
  private final CommandGateway commandGateway;

  @QueryHandler
  public ElementTrlDTO getElementTrlByElementIdAndIso3Language(
      GetElementTrlByElementIdAndIso3LanguageQuery query) {
    Assert.notNull(query.getElementId(), "Element ID is mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var optElementTrl =
        repository.getByParentIdAndIso3Language(query.getElementId(), query.getIso3Language());
    if (optElementTrl.isPresent()) return converter.asDTO(optElementTrl.get(), null);
    else {
      var optDefaultElementTrl = repository.getByParentIdAndIsDefaultIsTrue(query.getElementId());

      CreateElementTrlCommand command = new CreateElementTrlCommand();

      ElementTrlDTO newElementTrlDTO = new ElementTrlDTO();
      newElementTrlDTO.setParentId(query.getElementId());
      newElementTrlDTO.setIso3Language(query.getIso3Language());
      newElementTrlDTO.setIsTranslated(false);

      if (optDefaultElementTrl.isPresent()) {
        newElementTrlDTO.setValue(optDefaultElementTrl.get().getValue());
        newElementTrlDTO.setTooltip(optDefaultElementTrl.get().getTooltip());
        newElementTrlDTO.setIsDefault(true);
      } else {
        var element = elementRepository.getById(query.getElementId());
        newElementTrlDTO.setValue(element.getName());
        newElementTrlDTO.setTooltip("");
        newElementTrlDTO.setIsDefault(false);
      }
      command.setEntity(newElementTrlDTO);
      UUID newElementTrlId = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);

      return converter.asDTO(repository.getById(newElementTrlId), null);
    }
  }

  @QueryHandler
  public ElementTrlDTO getByElementTrlByNameAndIso3Language(
      GetElementTrlByNameAndIso3LanguageQuery query) {
    Assert.notNull(query.getName(), "Name mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var loggerPrefix = getLoggerPrefix("getByElementTrlNameAndLanguage");

    Optional<Element> optElement = elementRepository.getByName(query.getName());
    Element element;
    if (optElement.isEmpty()) {
      warn(loggerPrefix, "Element '{0}' not found, create a new one", query.getName());
      ElementDTO elementDTO = new ElementDTO();
      elementDTO.setName(query.getName());
      elementDTO.setIsTranslated(false);

      ElementTrlDTO newElementTrlDTO = new ElementTrlDTO();
      newElementTrlDTO.setIso3Language(query.getIso3Language());
      newElementTrlDTO.setIsDefault(true);
      newElementTrlDTO.setIsTranslated(false);
      newElementTrlDTO.setValue(query.getName());

      elementDTO.getTranslations().add(newElementTrlDTO);

      CreateElementCommand command = new CreateElementCommand();
      command.setEntity(elementDTO);
      UUID newElementId = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);
      element = elementRepository.getById(newElementId);
    } else {
      element = optElement.get();
    }
    return getElementTrlByElementIdAndIso3Language(
        new GetElementTrlByElementIdAndIso3LanguageQuery(element.getId(), query.getIso3Language()));
  }

  @QueryHandler
  public List<ElementTrlDTO> getElementTrlsByIso3Language(GetElementTrlsByIso3LanguageQuery query) {
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    return converter.asDTOList(repository.findByIso3Language(query.getIso3Language()), null);
  }

  @QueryHandler
  public List<ElementTrlDTO> getElementTrlsByElementId(GetElementTrlsByElementIdQuery query) {
    Assert.notNull(query.getElementId(), "Element ID is mandatory");

    return converter.asDTOList(repository.findByParentId(query.getElementId()), null);
  }
}