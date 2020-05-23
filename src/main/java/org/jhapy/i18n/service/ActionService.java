package org.jhapy.i18n.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.jhapy.i18n.domain.Action;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */

public interface ActionService extends CrudService<Action> {

  Page<Action> findAnyMatching(String filter, Pageable pageable);

  Page<Action> findByNameLike(String name, Pageable pageable);

  long countAnyMatching(String filter);

  long countByNameLike(String name);
}
