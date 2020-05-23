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
import org.jhapy.i18n.listeners.ElementTrlListener;
import org.jhapy.i18n.listeners.MessageTrlListener;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@TableGenerator(name = "MessageTrlKeyGen", table = "Sequence", pkColumnName = "COLUMN_NAME", pkColumnValue = "MESSAGE_TRL_ID", valueColumnName = "SEQ_VAL", initialValue = 0, allocationSize = 1)
@EntityListeners(MessageTrlListener.class)
public class MessageTrl extends EntityTranslation {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "MessageTrlKeyGen")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "MESSAGE_ID", nullable = false)
  private Message message;

  /**
   * ChatMessage Value
   */
  private String value;
}
