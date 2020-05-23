package org.jhapy.i18n.service;

import java.util.List;
import org.jhapy.i18n.domain.ElementTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
public interface ElementTrlService extends CrudService<ElementTrl> {

  List<ElementTrl> findByElement(Long elementId);

  long countByElement(Long elementId);

  ElementTrl getByNameAndIso3Language(String name, String iso3Language);

  List<ElementTrl> getByIso3Language(String iso3Language);

  List<ElementTrl> saveAll(List<ElementTrl> translations);

  void deleteAll(List<ElementTrl> elementTrls);

  void postUpdate(ElementTrl elementTrl);

  String importExcelFile(byte[] content);
}
