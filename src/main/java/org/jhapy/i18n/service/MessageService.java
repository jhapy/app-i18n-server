package org.jhapy.i18n.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.jhapy.i18n.domain.Message;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */

public interface MessageService extends CrudService<Message> {

  Page<Message> findAnyMatching(String filter, Pageable pageable);

  Page<Message> findByNameLike(String name, Pageable pageable);

  long countAnyMatching(String filter);

  long countByNameLike(String name);
}
