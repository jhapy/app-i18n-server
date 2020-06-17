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

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.config.AppProperties;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.SpringProfileConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration
@EnableCaching
public class CacheConfiguration implements DisposableBean, HasLogger {

  private final Environment env;

  private final ServerProperties serverProperties;

  private final DiscoveryClient discoveryClient;

  private final AppProperties appProperties;

  private Registration registration;

  public CacheConfiguration(Environment env, ServerProperties serverProperties,
      DiscoveryClient discoveryClient,
      AppProperties appProperties) {
    this.env = env;
    this.serverProperties = serverProperties;
    this.discoveryClient = discoveryClient;
    this.appProperties = appProperties;
  }

  @Autowired(required = false)
  public void setRegistration(Registration registration) {
    this.registration = registration;
  }

  @Override
  public void destroy() throws Exception {
    String loggerPrefix = getLoggerPrefix("destroy");
    logger().info(loggerPrefix+"Closing Cache Manager");
    Hazelcast.shutdownAll();
  }

  @Bean
  public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
    String loggerPrefix = getLoggerPrefix("cacheManager");
    logger().info(loggerPrefix+"Starting HazelcastCacheManager");
    return new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastInstance);
  }

  @Bean
  public HazelcastInstance hazelcastInstance(AppProperties appProperties) {
    String loggerPrefix = getLoggerPrefix("hazelcastInstance");

    logger().info(loggerPrefix+"Configuring Hazelcast");
    HazelcastInstance hazelCastInstance = Hazelcast.getHazelcastInstanceByName(env.getProperty("spring.application.name"));
    if (hazelCastInstance != null) {
      logger().info(loggerPrefix+"Hazelcast already initialized");
      return hazelCastInstance;
    }
    Config config = new Config();
    config.setInstanceName(env.getProperty("spring.application.name"));

    GroupConfig groupConfig = new GroupConfig();
    groupConfig.setName(env.getProperty("spring.application.name"));
    config.setGroupConfig(groupConfig);

    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
    if (this.registration == null) {
      logger().warn(loggerPrefix+"No discovery service is set up, Hazelcast cannot create a cluster.");
    } else {
      // The serviceId is by default the application's name,
      // see the "spring.application.name" standard Spring property
      String serviceId = registration.getServiceId();
      logger().debug(loggerPrefix+"Configuring Hazelcast clustering for instanceId: {}", serviceId);
      // In development, everything goes through 127.0.0.1, with a different port
      if (env
          .acceptsProfiles(Profiles.of(SpringProfileConstants.SPRING_PROFILE_TEST,SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT,
              SpringProfileConstants.SPRING_PROFILE_STAGING,SpringProfileConstants.SPRING_PROFILE_PRODUCTION))) {
        logger().debug("Application is running with the \"docker swarm\" profile, Hazelcast cluster will use Eureka Client");

        config.setProperty("hazelcast.discovery.enabled", "true");
        config.setProperty("hazelcast.shutdownhook.enabled", "true");
        config.setProperty("hazelcast.socket.bind.any", "false");

        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);

        if (StringUtils.isNotBlank(appProperties.getHazelcast().getInterfaces())) {
          InterfacesConfig interfaceConfig = config.getNetworkConfig().getInterfaces();
          interfaceConfig.setEnabled(true)
              .addInterface(appProperties.getHazelcast().getInterfaces());
          logger().info(
              loggerPrefix + "Use specific address : " + config.getNetworkConfig().getInterfaces()
                  .toString());
        }
      }else {
        logger().debug(loggerPrefix+"Application is running with the \"local\" profile, Hazelcast " +
            "cluster will only work with localhost instances");

        System.setProperty("hazelcast.local.localAddress", "127.0.0.1");
        config.getNetworkConfig().setPort(serverProperties.getPort() + 5701);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        for (ServiceInstance instance : discoveryClient.getInstances(serviceId)) {
          String clusterMember = "127.0.0.1:" + (instance.getPort() + 5701);
          logger().debug("Adding Hazelcast (dev) cluster member {}", clusterMember);
          config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(clusterMember);
        }
      }
    }
    config.getMapConfigs().put("default", initializeDefaultMapConfig(appProperties));

    // Full reference is available at: http://docs.hazelcast.org/docs/management-center/3.9/manual/html/Deploying_and_Starting.html
    config.setManagementCenterConfig(initializeDefaultManagementCenterConfig(appProperties));

    return Hazelcast.newHazelcastInstance(config);
  }

  private ManagementCenterConfig initializeDefaultManagementCenterConfig(
      AppProperties appProperties) {
    ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
    managementCenterConfig
        .setEnabled(appProperties.getHazelcast().getManagementCenter().isEnabled());
    managementCenterConfig.setUrl(appProperties.getHazelcast().getManagementCenter().getUrl());
    managementCenterConfig
        .setUpdateInterval(appProperties.getHazelcast().getManagementCenter().getUpdateInterval());
    return managementCenterConfig;
  }

  private MapConfig initializeDefaultMapConfig(AppProperties appProperties) {
    MapConfig mapConfig = new MapConfig();
    mapConfig.setStatisticsEnabled(true);
        /*
        Number of backups. If 1 is set as the backup-count for example,
        then all entries of the map will be copied to another JVM for
        fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
        */
    mapConfig.setBackupCount(appProperties.getHazelcast().getBackupCount());

        /*
        Valid values are:
        NONE (no eviction),
        LRU (Least Recently Used),
        LFU (Least Frequently Used).
        NONE is the default.
        */
    mapConfig.setEvictionPolicy(EvictionPolicy.LRU);

        /*
        Maximum size of the map. When max size is reached,
        map is evicted based on the policy defined.
        Any integer between 0 and Integer.MAX_VALUE. 0 means
        Integer.MAX_VALUE. Default is 0.
        */
    mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE));

    return mapConfig;
  }

  private MapConfig initializeDomainMapConfig(AppProperties appProperties) {
    MapConfig mapConfig = new MapConfig();
    mapConfig.setTimeToLiveSeconds(appProperties.getHazelcast().getTimeToLiveSeconds());
    return mapConfig;
  }
}
