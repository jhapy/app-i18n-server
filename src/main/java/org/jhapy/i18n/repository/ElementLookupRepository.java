package org.jhapy.i18n.repository;

import org.jhapy.i18n.domain.ElementLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ElementLookupRepository extends JpaRepository<ElementLookup, UUID> {
  Optional<ElementLookup> findByElementIdOrName(UUID elementId, String name);

  Optional<ElementLookup> findByName(String name);
}
