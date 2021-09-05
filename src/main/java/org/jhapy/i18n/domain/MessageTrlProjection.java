package org.jhapy.i18n.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class MessageTrlProjection implements Serializable {
  private String name;
  private String value;
  private Boolean isDefault;
  private Boolean isTranslated;
  private Long relatedEntityId;

  public MessageTrlProjection(
      String name, String value, Boolean isDefault, Boolean isTranslated, Long relatedEntityId) {
    this.name = name;
    this.value = value;
    this.isDefault = isDefault;
    this.isTranslated = isTranslated;
    this.relatedEntityId = relatedEntityId;
  }
}