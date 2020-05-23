package org.jhapy.i18n.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Repository
public interface MessageTrlRepository extends JpaRepository<MessageTrl, Long> {

  List<MessageTrl> findByMessage(Message message);

  long countByMessage(Message message);

  List<MessageTrl> findByIso3Language(String iso3Language);

  @Query("SELECT distinct(iso3Language) from MessageTrl order by iso3Language")
  List<String> getIso3Languages();

  Optional<MessageTrl> getByMessageAndIso3Language(Message message, String iso3Language);

  Optional<MessageTrl> getByMessageAndIsDefault(Message message, Boolean isDefault);
}
