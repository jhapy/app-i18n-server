package org.jhapy.i18n.service;

import java.util.List;
import org.jhapy.i18n.domain.MessageTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
public interface MessageTrlService extends CrudService<MessageTrl> {

  List<MessageTrl> findByMessage(Long messageId);

  long countByMessage(Long messageId);

  MessageTrl getByNameAndIso3Language(String name, String iso3Language);

  List<MessageTrl> getByIso3Language(String iso3Language);

  List<MessageTrl> saveAll(List<MessageTrl> translations);

  void deleteAll(List<MessageTrl> messageTrls);

  void postUpdate(MessageTrl messageTrl);

  String importExcelFile(byte[] content);
}
