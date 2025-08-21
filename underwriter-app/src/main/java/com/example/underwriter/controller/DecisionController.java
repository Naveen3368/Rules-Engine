package com.example.underwriter.controller;

import com.example.underwriter.model.UnderwriteResult;
import com.example.underwriter.service.EngineService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/decide")
public class DecisionController {
  private final EngineService engineService;
  public DecisionController(EngineService engineService){ this.engineService = engineService; }

  @PostMapping(value="/{domain}/{product}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UnderwriteResult> decide(@PathVariable String domain, @PathVariable String product,
                                                 @RequestParam(defaultValue="default") String clientId,
                                                 @RequestBody String payloadJson) {
    return ResponseEntity.ok(engineService.decide(domain, product, clientId, payloadJson));
  }

  @GetMapping("/health")
  public String health(){ return "OK"; }
}
