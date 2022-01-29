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

package org.jhapy.i18n.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories("org.jhapy.i18n.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EntityScan({
  "org.jhapy.i18n.domain",
  "org.axonframework.eventhandling.tokenstore",
  "org.axonframework.modelling.saga.repository.jpa",
  "org.axonframework.eventsourcing.eventstore.jpa"
})
@EnableTransactionManagement
public class DatabaseConfiguration {

  private final DataSource dataSource;

  private final MeterRegistry meterRegistry;

  public DatabaseConfiguration(DataSource dataSource, MeterRegistry meterRegistry) {
    this.dataSource = dataSource;
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  public void setUpHikariWithMetrics() {
    if (dataSource instanceof HikariDataSource) {
      ((HikariDataSource) dataSource).setMetricRegistry(meterRegistry);
    }
  }
}
