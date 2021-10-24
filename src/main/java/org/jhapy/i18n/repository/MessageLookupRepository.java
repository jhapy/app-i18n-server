package org.jhapy.i18n.repository;

import org.jhapy.i18n.domain.MessageLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageLookupRepository extends JpaRepository<MessageLookup, UUID> {
  Optional<MessageLookup> findByMessageIdOrName(UUID messageID, String name);

  Optional<MessageLookup> findByName(String name);
}