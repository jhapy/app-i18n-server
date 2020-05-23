package org.jhapy.i18n.domain;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jhapy.i18n.listeners.ActionTrlListener;
import org.jhapy.i18n.listeners.ElementTrlListener;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@TableGenerator(name = "ElementTrlKeyGen", table = "Sequence", pkColumnName = "COLUMN_NAME", pkColumnValue = "ELEMENT_TRL_ID", valueColumnName = "SEQ_VAL", initialValue = 0, allocationSize = 1)
@EntityListeners(ElementTrlListener.class)
public class ElementTrl extends EntityTranslation {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "ElementTrlKeyGen")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "ELEMENT_ID", nullable = false)
  private Element element;
  /**
   * Element Value
   */
  private String value;

  private String tooltip;
}
