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

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@TableGenerator(name = "ActionTrlKeyGen", table = "Sequence", pkColumnName = "COLUMN_NAME", pkColumnValue = "ACTION_TRL_ID", valueColumnName = "SEQ_VAL", initialValue = 0, allocationSize = 1)
@EntityListeners(ActionTrlListener.class)
public class ActionTrl extends EntityTranslation {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "ActionTrlKeyGen")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "ACTION_ID", nullable = false)
  private Action action;

  /**
   * Action Value
   */
  private String value;

  private String tooltip;
}
