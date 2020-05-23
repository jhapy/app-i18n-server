package org.jhapy.i18n.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-08-06
 */
@RestController
public class HealthEndpoint {

  @GetMapping(value = "/ping", produces = "application/txt")
  public ResponseEntity<?> ping() {
    return ResponseEntity.ok("pong");
  }
}
