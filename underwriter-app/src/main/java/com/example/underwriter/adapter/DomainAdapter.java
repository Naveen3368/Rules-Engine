package com.example.underwriter.adapter;

import com.example.underwriter.model.FactsEnvelope;
import com.fasterxml.jackson.databind.JsonNode;

public interface DomainAdapter {
  FactsEnvelope normalize(String product, String clientId, JsonNode payload);
}
