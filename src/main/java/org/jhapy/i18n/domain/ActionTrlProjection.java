package org.jhapy.i18n.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ActionTrlProjection implements Serializable {
  private String name;
  private String value;
  private String tooltip;
  private Boolean isDefault;
  private Boolean isTranslated;
  private Long relatedEntityId;

  public ActionTrlProjection(
      String name,
      String value,
      String tooltip,
      Boolean isDefault,
      Boolean isTranslated,
      Long relatedEntityId) {
    this.name = name;
    this.value = value;
    this.tooltip = tooltip;
    this.isDefault = isDefault;
    this.isTranslated = isTranslated;
    this.relatedEntityId = relatedEntityId;
  }
}