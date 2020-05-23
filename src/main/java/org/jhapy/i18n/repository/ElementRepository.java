package org.jhapy.i18n.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.jhapy.i18n.domain.Element;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Repository
public interface ElementRepository extends JpaRepository<Element, Long> {

  Optional<Element> getByName(String name);

  Page<Element> findByNameLike(String name, Pageable pageable);

  long countByNameLike(String name);

  @Query("SELECT DISTINCT a FROM Element a INNER JOIN ElementTrl t ON a.id = t.element.id WHERE a.name like :filter or t.value like :filter")
  Page<Element> findAnyMatching(String filter, Pageable pageable);

  @Query("SELECT COUNT(DISTINCT a) FROM Element a INNER JOIN ElementTrl t ON a.id = t.element.id WHERE a.name like :filter or t.value like :filter")
  long countAnyMatching(String filter);
}
