package com.example.underwriter.model;

import java.util.HashMap;
import java.util.Map;

public class FactsEnvelope {
  private String domain;
  private String product;
  private String clientId;
  private Map<String,Object> attributes = new HashMap<>();
  private Map<String,Object> derived = new HashMap<>();
  private Map<String,Object> config = new HashMap<>();
  private UnderwriteResult result = new UnderwriteResult();
  public String getDomain(){return domain;} public void setDomain(String d){domain=d;}
  public String getProduct(){return product;} public void setProduct(String p){product=p;}
  public String getClientId(){return clientId;} public void setClientId(String c){clientId=c;}
  public Map<String,Object> getAttributes(){return attributes;} public void setAttributes(Map<String,Object> a){attributes=a;}
  public Map<String,Object> getDerived(){return derived;} public void setDerived(Map<String,Object> d){derived=d;}
  public Map<String,Object> getConfig(){return config;} public void setConfig(Map<String,Object> c){config=c;}
  public UnderwriteResult getResult(){return result;} public void setResult(UnderwriteResult r){result=r;}
}
