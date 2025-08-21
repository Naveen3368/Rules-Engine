package com.example.underwriter.adapter;

import com.example.underwriter.model.FactsEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap; import java.util.Map;

public class AutoAdapter implements DomainAdapter {
  public FactsEnvelope normalize(String product, String clientId, JsonNode p){
    FactsEnvelope env = new FactsEnvelope();
    env.setDomain("auto"); env.setProduct(product); env.setClientId(clientId);
    Map<String,Object> a = new HashMap<>();
    a.put("driverAge", jInt(p,"driverAge"));
    a.put("incidentsLast3Years", jInt(p,"incidentsLast3Years"));
    a.put("vehicleValue", jD(p,"vehicleValue"));
    a.put("desiredCoverage", jD(p,"desiredCoverage"));
    a.put("annualMileage", jInt(p,"annualMileage"));
    env.setAttributes(a); env.setDerived(new HashMap<>());
    return env;
  }
  private Integer jInt(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asInt():null;}
  private Double jD(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asDouble():null;}
}
