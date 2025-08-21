package com.example.underwriter.adapter;

import com.example.underwriter.model.FactsEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap; import java.util.Map;

public class TravelAdapter implements DomainAdapter {
  public FactsEnvelope normalize(String product, String clientId, JsonNode p){
    FactsEnvelope env = new FactsEnvelope();
    env.setDomain("travel"); env.setProduct(product); env.setClientId(clientId);
    Map<String,Object> a = new HashMap<>();
    a.put("age", jInt(p,"age"));
    a.put("tripDays", jInt(p,"tripDays"));
    a.put("destinationRisk", jTxt(p,"destinationRisk"));
    a.put("sumInsured", jD(p,"sumInsured"));
    a.put("preExistingConditions", jBool(p,"preExistingConditions"));
    a.put("pastClaims", jInt(p,"pastClaims"));
    env.setAttributes(a); env.setDerived(new HashMap<>());
    return env;
  }
  private Integer jInt(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asInt():null;}
  private Double jD(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asDouble():null;}
  private String jTxt(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asText():null;}
  private Boolean jBool(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asBoolean():null;}
}
