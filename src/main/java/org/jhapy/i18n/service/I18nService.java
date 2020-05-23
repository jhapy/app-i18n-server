package org.jhapy.i18n.service;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 11/04/2020
 */
public interface I18nService {

  Byte[] getI18NFile();

  String importI18NFile(Byte[] fileToImport);
}
