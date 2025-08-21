package com.example.underwriter.service;

import com.example.underwriter.adapter.*;
import com.example.underwriter.config.RulesRegistry;
import com.example.underwriter.model.FactsEnvelope;
import com.example.underwriter.model.UnderwriteResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.Agenda;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class EngineService {
  private final RulesRegistry rulesRegistry;
  private final ConfigStore configStore;
  private final ObjectMapper mapper = new ObjectMapper();
  public EngineService(RulesRegistry rulesRegistry, ConfigStore configStore){ this.rulesRegistry = rulesRegistry; this.configStore = configStore; }
  public UnderwriteResult decide(String domain, String product, String clientId, String payloadJson){
    try {
      JsonNode node = mapper.readTree(payloadJson);
      DomainAdapter adapter = adapter(domain);
      FactsEnvelope env = adapter.normalize(product, clientId, node);
      Map<String,Object> cfg = configStore.load(domain, product, clientId);
      env.setConfig(cfg);
      KieContainer kc = rulesRegistry.containerFor(domain);
      String sessionName = domain + "-session";
      KieSession ks = kc.newKieSession(sessionName);
      if (ks == null) {
        throw new RuntimeException("Could not create KieSession '" + sessionName + "' for domain: " + domain);
      }
      try {
        ks.setGlobal("config", env.getConfig());
        ks.insert(env);
        Agenda a = ks.getAgenda();
        a.getAgendaGroup("finalize").setFocus();
        a.getAgendaGroup("risk").setFocus();
        a.getAgendaGroup("affordability").setFocus();
        a.getAgendaGroup("eligibility").setFocus();
        ks.fireAllRules();
      } finally {
        if (ks != null) {
          ks.dispose();
        }
      }
      return env.getResult();
    } catch (Exception e) {
      UnderwriteResult r = new UnderwriteResult();
      r.setStatus(UnderwriteResult.Status.MANUAL_REVIEW);
      r.addReason("Engine error: " + e.getMessage());
      return r;
    }
  }
  private DomainAdapter adapter(String domain){
    switch (domain.toLowerCase()) {
      case "mortgage": return new MortgageAdapter();
      case "travel": return new TravelAdapter();
      case "auto": return new AutoAdapter();
      case "life": return new LifeAdapter();
      default: throw new IllegalArgumentException("Unsupported domain: " + domain);
    }
  }
}
