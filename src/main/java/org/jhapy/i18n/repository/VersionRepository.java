/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.i18n.repository;

import java.util.List;
import org.jhapy.i18n.domain.I18NVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-16
 */
@Repository
public interface VersionRepository extends JpaRepository<I18NVersion, Long> {

  @Query("SELECT v.recordVersion FROM I18NVersion v WHERE v.tableName = 'Element' and v.isoLang = :isoLang")
  Integer getElementCurrentVersion(String isoLang);

  @Query("SELECT v.recordVersion FROM I18NVersion v WHERE v.tableName = 'Element'")
  List<Integer> getElementCurrentVersion();

  @Query("SELECT v FROM I18NVersion v WHERE v.tableName = 'Element'")
  List<I18NVersion> getElementVersions();

  @Query("SELECT v.recordVersion FROM I18NVersion v WHERE v.tableName = 'Action' and v.isoLang = :isoLang")
  Integer getActionCurrentVersion(String isoLang);

  @Query("SELECT v FROM I18NVersion v WHERE v.tableName = 'Action' and v.isoLang = :isoLang")
  I18NVersion getActionByIsoLang(String isoLang);

  @Query("SELECT v.recordVersion FROM I18NVersion v WHERE v.tableName = 'Action'")
  List<Integer> getActionCurrentVersions();

  @Query("SELECT v FROM I18NVersion v WHERE v.tableName = 'Action'")
  List<I18NVersion> getActionVersions();

  @Query("SELECT v.recordVersion FROM I18NVersion v WHERE v.tableName = 'Message' and v.isoLang = :isoLang")
  Integer getMessageCurrentVersion(String isoLang);

  @Query("SELECT v.recordVersion FROM I18NVersion v WHERE v.tableName = 'Message'")
  List<Integer> getMessageCurrentVersions();

  @Query("SELECT v FROM I18NVersion v WHERE v.tableName = 'Message'")
  List<I18NVersion> getMessageVersions();

  @Modifying
  @Query("UPDATE I18NVersion SET previousRecordVersion = recordVersion, recordVersion = previousRecordVersion + 1, notificationSent = false WHERE tableName = 'Action' and isoLang = :isoLang")
  void incActionRecords(String isoLang);

  @Modifying
  @Query("UPDATE I18NVersion SET previousRecordVersion = recordVersion, recordVersion = previousRecordVersion + 1, notificationSent = false WHERE tableName = 'Action'")
  void incActionRecords();

  @Modifying
  @Query("UPDATE I18NVersion SET previousRecordVersion = recordVersion, recordVersion = previousRecordVersion + 1, notificationSent = false WHERE tableName = 'Element' and isoLang = :isoLang")
  void incElementRecords(String isoLang);

  @Modifying
  @Query("UPDATE I18NVersion SET previousRecordVersion = recordVersion, recordVersion = previousRecordVersion + 1, notificationSent = false WHERE tableName = 'Element'")
  void incElementRecords();

  @Modifying
  @Query("UPDATE I18NVersion SET previousRecordVersion = recordVersion, recordVersion = previousRecordVersion + 1, notificationSent = false WHERE tableName = 'Message' and isoLang = :isoLang")
  void incMessageRecords(String isoLang);

  @Modifying
  @Query("UPDATE I18NVersion SET previousRecordVersion = recordVersion, recordVersion = previousRecordVersion + 1, notificationSent = false WHERE tableName = 'Message'")
  void incMessageRecords();
}
