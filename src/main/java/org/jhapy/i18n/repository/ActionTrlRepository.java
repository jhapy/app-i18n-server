package org.jhapy.i18n.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.jhapy.i18n.domain.Action;
import org.jhapy.i18n.domain.ActionTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Repository
public interface ActionTrlRepository extends JpaRepository<ActionTrl, Long> {

  List<ActionTrl> findByAction(Action action);

  long countByAction(Action action);

  List<ActionTrl> findByIso3Language(String iso3Language);

  @Query("SELECT distinct(iso3Language) from ActionTrl order by iso3Language")
  List<String> getIso3Languages();

  Optional<ActionTrl> getByActionAndIso3Language(Action action, String iso3Language);

  Optional<ActionTrl> getByActionAndIsDefault(Action action, Boolean isDefault);
}
