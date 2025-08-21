package com.example.underwriter.adapter;

import com.example.underwriter.model.FactsEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap; import java.util.Map;

public class MortgageAdapter implements DomainAdapter {
  public FactsEnvelope normalize(String product, String clientId, JsonNode p){
    FactsEnvelope env = new FactsEnvelope();
    env.setDomain("mortgage"); env.setProduct(product); env.setClientId(clientId);
    Map<String,Object> a = new HashMap<>();
    a.put("creditScore", jInt(p,"creditScore"));
    a.put("age", jInt(p,"age"));
    a.put("annualIncome", jD(p,"annualIncome"));
    a.put("monthlyDebt", jD(p,"monthlyDebt"));
    a.put("employmentYears", jD(p,"employmentYears"));
    a.put("propertyValue", jD(p,"propertyValue"));
    if (p.has("loanAmount")) { a.put("loanAmount", jD(p,"loanAmount")); 
      Double pv=tD(a.get("propertyValue")), la=tD(a.get("loanAmount")); if(pv!=null && la!=null) a.put("downPayment", pv-la); }
    else if (p.has("downPayment")) { a.put("downPayment", jD(p,"downPayment"));
      Double pv=tD(a.get("propertyValue")), dp=tD(a.get("downPayment")); if(pv!=null && dp!=null) a.put("loanAmount", pv-dp); }
    env.setAttributes(a);
    Map<String,Object> d = new HashMap<>();
    Double income=tD(a.get("annualIncome")), md=tD(a.get("monthlyDebt")); if(income!=null && income>0 && md!=null) d.put("dti", md/(income/12.0));
    Double pv=tD(a.get("propertyValue")), la=tD(a.get("loanAmount")); if(pv!=null && pv>0 && la!=null) d.put("ltv", la/pv);
    env.setDerived(d);
    return env;
  }
  private Integer jInt(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asInt():null;}
  private Double jD(JsonNode n,String k){return n.has(k)&&!n.get(k).isNull()?n.get(k).asDouble():null;}
  private Double tD(Object o){ if(o==null)return null; if(o instanceof Number) return ((Number)o).doubleValue(); try{return Double.parseDouble(String.valueOf(o));}catch(Exception e){return null;}}
}
