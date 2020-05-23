package org.jhapy.i18n.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jhapy.i18n.domain.Element;
import org.jhapy.i18n.domain.ElementTrl;
import org.jhapy.i18n.repository.ElementRepository;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Service
@Transactional(readOnly = true)
public class ElementServiceImpl implements ElementService {

  private final ElementRepository elementRepository;
  private final ElementTrlService elementTrlService;

  public ElementServiceImpl(ElementRepository elementRepository,
      ElementTrlService elementTrlService) {
    this.elementRepository = elementRepository;
    this.elementTrlService = elementTrlService;
  }

  @Override
  public Page<Element> findByNameLike(String name, Pageable pageable) {
    return elementRepository.findByNameLike(name, pageable);
  }

  @Override
  public long countByNameLike(String name) {
    return elementRepository.countByNameLike(name);
  }

  @Override
  public Page<Element> findAnyMatching(String filter, Pageable pageable) {
    if (StringUtils.isBlank(filter)) {
      return elementRepository.findAll(pageable);
    } else {
      return elementRepository.findAnyMatching(filter, pageable);
    }
  }

  @Override
  public long countAnyMatching(String filter) {
    if (StringUtils.isBlank(filter)) {
      return elementRepository.count();
    } else {
      return elementRepository.countAnyMatching(filter);
    }
  }

  @Override
  @Transactional
  public Element save(Element entity) {
    List<ElementTrl> translations = entity.getTranslations();
    entity = elementRepository.save(entity);
    for (ElementTrl elementTrl : translations) {
      elementTrl.setElement(entity);
    }
    if (translations.size() > 0) {
      entity.setTranslations(elementTrlService.saveAll(translations));
    }

    return entity;
  }

  @Override
  @Transactional
  public void delete(Element entity) {
    List<ElementTrl> elementTrls = elementTrlService.findByElement(entity.getId());
    if (elementTrls.size() > 0) {
      elementTrlService.deleteAll(elementTrls);
    }

    elementRepository.delete(entity);
  }

  @Override
  public JpaRepository<Element, Long> getRepository() {
    return elementRepository;
  }
}
