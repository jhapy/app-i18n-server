package org.jhapy.i18n.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jhapy.i18n.domain.Message;
import org.jhapy.i18n.domain.MessageTrl;
import org.jhapy.i18n.repository.MessageRepository;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Service
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final MessageTrlService messageTrlService;

  public MessageServiceImpl(MessageRepository messageRepository,
      MessageTrlService messageTrlService) {
    this.messageRepository = messageRepository;
    this.messageTrlService = messageTrlService;
  }

  @Override
  public Page<Message> findByNameLike(String name, Pageable pageable) {
    return messageRepository.findByNameLike(name, pageable);
  }

  @Override
  public long countByNameLike(String name) {
    return messageRepository.countByNameLike(name);
  }

  @Override
  public Page<Message> findAnyMatching(String filter, Pageable pageable) {
    if (StringUtils.isBlank(filter)) {
      return messageRepository.findAll(pageable);
    } else {
      return messageRepository.findAnyMatching(filter, pageable);
    }
  }

  @Override
  public long countAnyMatching(String filter) {
    if (StringUtils.isBlank(filter)) {
      return messageRepository.count();
    } else {
      return messageRepository.countAnyMatching(filter);
    }
  }

  @Override
  @Transactional
  public Message save(Message entity) {
    List<MessageTrl> translations = entity.getTranslations();
    entity = messageRepository.save(entity);
    for (MessageTrl messageTrl : translations) {
      messageTrl.setMessage(entity);
    }
    if (translations.size() > 0) {
      entity.setTranslations(messageTrlService.saveAll(translations));
    }

    return entity;
  }

  @Override
  @Transactional
  public void delete(Message entity) {
    List<MessageTrl> messageTrls = messageTrlService.findByMessage(entity.getId());
    if (messageTrls.size() > 0) {
      messageTrlService.deleteAll(messageTrls);
    }

    messageRepository.delete(entity);
  }

  @Override
  public JpaRepository<Message, Long> getRepository() {
    return messageRepository;
  }
}
