package org.jhapy.i18n.converter;

import java.util.Collection;
import java.util.List;
import org.jhapy.commons.converter.CommonsConverterV2;
import org.jhapy.dto.domain.i18n.Action;
import org.jhapy.dto.domain.i18n.ActionTrl;
import org.jhapy.dto.domain.i18n.Element;
import org.jhapy.dto.domain.i18n.ElementTrl;
import org.jhapy.dto.domain.i18n.Message;
import org.jhapy.dto.domain.i18n.MessageTrl;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 18/05/2021
 */
@Mapper(componentModel = "spring")
public abstract class I18NConverterV2 extends CommonsConverterV2 {

  public abstract Action convertToDto(org.jhapy.i18n.domain.Action domain);

  public abstract org.jhapy.i18n.domain.Action convertToDomain(Action dto);

  public abstract List<Action> convertToDtoActions(
      Collection<org.jhapy.i18n.domain.Action> domains);

  public abstract List<org.jhapy.i18n.domain.Action> convertToDomainActions(
      Collection<Action> dtos);

  public abstract ActionTrl convertToDto(org.jhapy.i18n.domain.ActionTrl domain);

  public abstract org.jhapy.i18n.domain.ActionTrl convertToDomain(ActionTrl dto);

  public abstract List<ActionTrl> convertToDtoActionTrls(
      Collection<org.jhapy.i18n.domain.ActionTrl> domains);

  public abstract List<org.jhapy.i18n.domain.ActionTrl> convertToDomainActionTrls(
      Collection<ActionTrl> dtos);

  public abstract Element convertToDto(org.jhapy.i18n.domain.Element domain);

  public abstract org.jhapy.i18n.domain.Element convertToDomain(Element dto);

  public abstract List<Element> convertToDtoElements(
      Collection<org.jhapy.i18n.domain.Element> domains);

  public abstract List<org.jhapy.i18n.domain.Element> convertToDomainElements(
      Collection<Element> dtos);

  public abstract ElementTrl convertToDto(org.jhapy.i18n.domain.ElementTrl domain);

  public abstract org.jhapy.i18n.domain.ElementTrl convertToDomain(ElementTrl dto);

  public abstract List<ElementTrl> convertToDtoElementTrls(
      Collection<org.jhapy.i18n.domain.ElementTrl> domains);

  public abstract List<org.jhapy.i18n.domain.ElementTrl> convertToDomainElementTrls(
      Collection<ElementTrl> dtos);

  public abstract Message convertToDto(org.jhapy.i18n.domain.Message domain);

  public abstract org.jhapy.i18n.domain.Message convertToDomain(Message dto);

  public abstract List<Message> convertToDtoMessages(
      Collection<org.jhapy.i18n.domain.Message> domains);

  public abstract List<org.jhapy.i18n.domain.Message> convertToDomainMessages(
      Collection<Message> dtos);

  public abstract MessageTrl convertToDto(org.jhapy.i18n.domain.MessageTrl domain);

  public abstract org.jhapy.i18n.domain.MessageTrl convertToDomain(MessageTrl dto);

  public abstract List<MessageTrl> convertToDtoMessageTrls(
      Collection<org.jhapy.i18n.domain.MessageTrl> domains);

  public abstract List<org.jhapy.i18n.domain.MessageTrl> convertToDomainMessageTrls(
      Collection<MessageTrl> dtos);

  @AfterMapping
  protected void afterConvert(org.jhapy.i18n.domain.ActionTrl dto,
      @MappingTarget ActionTrl domain) {
    domain.setName(dto.getAction().getName());
  }

  @AfterMapping
  protected void afterConvert(org.jhapy.i18n.domain.ElementTrl dto,
      @MappingTarget ElementTrl domain) {
    domain.setName(dto.getElement().getName());
  }

  @AfterMapping
  protected void afterConvert(org.jhapy.i18n.domain.MessageTrl dto,
      @MappingTarget MessageTrl domain) {
    domain.setName(dto.getMessage().getName());
  }
}
