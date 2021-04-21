package org.jhapy.i18n.service;

import org.jhapy.i18n.domain.I18NVersion;
import org.jhapy.i18n.repository.VersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 28/03/2021
 */
@Service
@Transactional(readOnly = true)
public class VersionServiceImpl implements VersionService {

  private final VersionRepository versionRepository;

  public VersionServiceImpl(VersionRepository versionRepository) {
    this.versionRepository = versionRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void incVersionForAction(String iso3Lang) {
    I18NVersion i18NVersion = versionRepository.getActionByIsoLang(iso3Lang);
    i18NVersion.setPreviousRecordVersion(i18NVersion.getRecordVersion());
    i18NVersion.setRecordVersion(i18NVersion.getRecordVersion() + 1);
    i18NVersion.setNotificationSent(false);
    versionRepository.save(i18NVersion);
  }
}
