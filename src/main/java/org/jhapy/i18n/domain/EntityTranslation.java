package org.jhapy.i18n.domain;

import javax.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Base class for all translations
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class EntityTranslation extends BaseEntity {

  /**
   * Is default translation
   */
  private Boolean isDefault;

  /**
   * Language
   */
  private String iso3Language;

  private Boolean isTranslated;
}
