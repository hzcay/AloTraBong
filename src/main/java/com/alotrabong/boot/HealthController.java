package com.alotrabong.boot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health")
@RestController
public class HealthController {

  @Operation(summary = "Ping server")
  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }

  @Operation(summary = "Health check đơn giản")
  @GetMapping("/healthz")
  public String healthz() {
    return "OK";
  }
}
