package com.example.underwriter.config;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.springframework.stereotype.Component;
import java.util.HashMap; 
import java.util.Map;

@Component
public class RulesRegistry {
  private final Map<String,KieContainer> containers = new HashMap<>();
  private final KieServices kieServices;
  
  public RulesRegistry() {
    this.kieServices = KieServices.Factory.get();
    initializeContainers();
  }
  
  private void initializeContainers() {
    try {
      // First, try to load rules dynamically from Maven repository
      if (tryDynamicLoading()) {
        return;
      }
      
      // Fallback to classpath loading if dynamic loading fails
      loadFromClasspath();
      
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize rule containers", e);
    }
  }
  
  private boolean tryDynamicLoading() {
    try {
      // Try to load each domain's rules dynamically
      loadDomainRules("mortgage", "com.example", "rules-mortgage", "0.2.1");
      loadDomainRules("travel", "com.example", "rules-travel", "0.2.1");
      loadDomainRules("auto", "com.example", "rules-auto", "0.2.1");
      loadDomainRules("life", "com.example", "rules-life", "0.2.1");
      return true;
    } catch (Exception e) {
      System.out.println("Dynamic loading failed, falling back to classpath: " + e.getMessage());
      return false;
    }
  }
  
  private void loadDomainRules(String domain, String groupId, String artifactId, String version) {
    try {
      ReleaseId releaseId = kieServices.newReleaseId(groupId, artifactId, version);
      KieContainer kc = kieServices.newKieContainer(releaseId);
      containers.put(domain, kc);
      System.out.println("Successfully loaded dynamic rules for domain: " + domain);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load dynamic rules for domain: " + domain, e);
    }
  }
  
  private void loadFromClasspath() {
    try {
      KieContainer kc = kieServices.getKieClasspathContainer();
      if (kc == null) {
        throw new RuntimeException("Could not create KieClasspathContainer");
      }
      
      // Store the same container for all domains
      // The rules will be filtered by domain in the rule conditions
      containers.put("mortgage", kc);
      containers.put("travel", kc);
      containers.put("auto", kc);
      containers.put("life", kc);
      
      System.out.println("Successfully loaded rules from classpath");
    } catch (Exception e) {
      throw new RuntimeException("Failed to load rules from classpath", e);
    }
  }
  public KieContainer containerFor(String domain){
    KieContainer kc = containers.get(domain.toLowerCase());
    if (kc == null) throw new IllegalArgumentException("Unknown domain: " + domain);
    return kc;
  }
}
