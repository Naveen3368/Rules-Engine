package com.example.underwriter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream; import java.util.HashMap; import java.util.Map;

@Component
public class ConfigStore {
  private final ObjectMapper mapper = new ObjectMapper();
  public Map<String,Object> load(String domain, String product, String clientId){
    String[] files = new String[] {
      String.format("config/%s/%s-%s.json", domain, product, clientId),
      String.format("config/%s/%s.json", domain, clientId),
      String.format("config/%s/default.json", domain)
    };
    for (String f : files) {
      try (InputStream in = new ClassPathResource(f).getInputStream()){
        return mapper.readValue(in, new TypeReference<Map<String,Object>>(){});
      } catch(Exception ignore){}
    }
    return new HashMap<>();
  }
}
