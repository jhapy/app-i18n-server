package org.jhapy.i18n.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private final Hazelcast hazelcast = new Hazelcast();

  @Data
  public static class Hazelcast {

    private final ManagementCenter managementCenter = new ManagementCenter();
    private String interfaces;
    private int timeToLiveSeconds = 3600;
    private int backupCount = 1;

    @Data
    public static class ManagementCenter {

      private boolean enabled = false;
      private int updateInterval = 3;
      private String url = "";
    }
  }
}
