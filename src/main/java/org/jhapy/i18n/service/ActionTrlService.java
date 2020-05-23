package org.jhapy.i18n.service;

import java.util.List;
import org.jhapy.i18n.domain.ActionTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
public interface ActionTrlService extends CrudService<ActionTrl> {

  List<ActionTrl> findByAction(Long actionId);

  long countByAction(Long actionId);

  ActionTrl getByNameAndIso3Language(String name, String iso3Language);

  List<ActionTrl> getByIso3Language(String iso3Language);

  List<ActionTrl> saveAll(List<ActionTrl> translations);

  void deleteAll(List<ActionTrl> actionTrls);

  void postUpdate(ActionTrl actionTrl);

  String importExcelFile(byte[] content);
}
