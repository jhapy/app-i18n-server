package org.jhapy.i18n.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class ElementTrlProjection implements Serializable {
  private String name;
  private String value;
  private String tooltip;
  private Boolean isDefault;
  private Boolean isTranslated;
  private Long relatedEntityId;

  public ElementTrlProjection(
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