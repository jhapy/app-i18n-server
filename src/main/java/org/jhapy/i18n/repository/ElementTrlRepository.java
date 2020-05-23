package org.jhapy.i18n.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Repository
public interface ElementTrlRepository extends JpaRepository<ElementTrl, Long> {

  List<ElementTrl> findByElement(Element element);

  List<ElementTrl> findByElementOrderByIso3Language(Element element);

  @Query("SELECT distinct(iso3Language) from ElementTrl order by iso3Language")
  List<String> getIso3Languages();

  long countByElement(Element element);

  List<ElementTrl> findByIso3Language(String iso3Language);

  Optional<ElementTrl> getByElementAndIso3Language(Element element, String iso3Language);

  Optional<ElementTrl> getByElementAndIsDefault(Element element, Boolean isDefault);
}
