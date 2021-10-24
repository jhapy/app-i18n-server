package org.jhapy.i18n.query;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.cqrs.command.i18n.CreateActionCommand;
import org.jhapy.cqrs.command.i18n.CreateActionTrlCommand;
import org.jhapy.cqrs.query.i18n.GetActionTrlByActionIdAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlByNameAndIso3LanguageQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByActionIdQuery;
import org.jhapy.cqrs.query.i18n.GetActionTrlsByIso3LanguageQuery;
import org.jhapy.dto.domain.i18n.ActionDTO;
import org.jhapy.dto.domain.i18n.ActionTrlDTO;
import org.jhapy.i18n.converter.ActionTrlConverter;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.repository.ActionRepository;
import org.jhapy.i18n.repository.ActionTrlRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ActionTrlQueryHandler implements HasLogger {
  private final ActionTrlRepository repository;
  private final ActionRepository actionRepository;
  private final ActionTrlConverter converter;
  private final CommandGateway commandGateway;

  @QueryHandler
  public ActionTrlDTO getActionTrlByActionIdAndIso3Language(
      GetActionTrlByActionIdAndIso3LanguageQuery query) {
    Assert.notNull(query.getActionId(), "Action ID is mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var optActionTrl =
        repository.getByParentIdAndIso3Language(query.getActionId(), query.getIso3Language());
    if (optActionTrl.isPresent()) return converter.asDTO(optActionTrl.get(), null);
    else {
      var optDefaultActionTrl = repository.getByParentIdAndIsDefaultIsTrue(query.getActionId());

      CreateActionTrlCommand command = new CreateActionTrlCommand();

      ActionTrlDTO newActionTrlDTO = new ActionTrlDTO();
      newActionTrlDTO.setParentId(query.getActionId());
      newActionTrlDTO.setIso3Language(query.getIso3Language());
      newActionTrlDTO.setIsTranslated(false);

      if (optDefaultActionTrl.isPresent()) {
        newActionTrlDTO.setValue(optDefaultActionTrl.get().getValue());
        newActionTrlDTO.setTooltip(optDefaultActionTrl.get().getTooltip());
        newActionTrlDTO.setIsDefault(true);
      } else {
        var action = actionRepository.getById(query.getActionId());
        newActionTrlDTO.setValue(action.getName());
        newActionTrlDTO.setTooltip("");
        newActionTrlDTO.setIsDefault(false);
      }
      command.setEntity(newActionTrlDTO);
      UUID newActionTrlId = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);

      return converter.asDTO(repository.getById(newActionTrlId), null);
    }
  }

  @QueryHandler
  public ActionTrlDTO getByActionTrlByNameAndIso3Language(
      GetActionTrlByNameAndIso3LanguageQuery query) {
    Assert.notNull(query.getName(), "Name mandatory");
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    var loggerPrefix = getLoggerPrefix("getByActionTrlNameAndLanguage");

    Optional<Action> optAction = actionRepository.getByName(query.getName());
    Action action;
    if (optAction.isEmpty()) {
      warn(loggerPrefix, "Action '{0}' not found, create a new one", query.getName());
      ActionDTO actionDTO = new ActionDTO();
      actionDTO.setName(query.getName());
      actionDTO.setIsTranslated(false);

      ActionTrlDTO newActionTrlDTO = new ActionTrlDTO();
      newActionTrlDTO.setIso3Language(query.getIso3Language());
      newActionTrlDTO.setIsDefault(true);
      newActionTrlDTO.setIsTranslated(false);
      newActionTrlDTO.setValue(query.getName());

      actionDTO.getTranslations().add(newActionTrlDTO);

      CreateActionCommand command = new CreateActionCommand();
      command.setEntity(actionDTO);
      UUID newActionId = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);
      action = actionRepository.getById(newActionId);
    } else {
      action = optAction.get();
    }
    return getActionTrlByActionIdAndIso3Language(
        new GetActionTrlByActionIdAndIso3LanguageQuery(action.getId(), query.getIso3Language()));
  }

  @QueryHandler
  public List<ActionTrlDTO> getActionTrlsByIso3Language(GetActionTrlsByIso3LanguageQuery query) {
    Assert.notNull(query.getIso3Language(), "ISO3 language is mandatory");

    return converter.asDTOList(repository.findByIso3Language(query.getIso3Language()), null);
  }

  @QueryHandler
  public List<ActionTrlDTO> getActionTrlsByActionId(GetActionTrlsByActionIdQuery query) {
    Assert.notNull(query.getActionId(), "Action ID is mandatory");

    return converter.asDTOList(repository.findByParentId(query.getActionId()), null);
  }
}