package org.jhapy.i18n.repository;

import org.jhapy.i18n.domain.ActionLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActionLookupRepository extends JpaRepository<ActionLookup, UUID> {
  Optional<ActionLookup> findByActionIdOrName(UUID actionId, String name);

  Optional<ActionLookup> findByName(String name);
}