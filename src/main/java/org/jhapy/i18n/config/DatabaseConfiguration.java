package org.jhapy.i18n.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("org.jhapy.i18n.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EntityScan("org.jhapy.i18n.domain")
@EnableTransactionManagement
public class DatabaseConfiguration {

}
