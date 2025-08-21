# Request/Response Flow for Rule Execution

This sequence diagram illustrates a rule execution process within a decision-making system, showing the interactions between various components from an initial API request to the final decision and response.

```mermaid
sequenceDiagram
    participant Client as API Client
    participant Controller as DecisionController
    participant Service as EngineService
    participant Registry as RulesRegistry
    participant KieContainer as KieContainer
    participant KieSession as KieSession
    participant Rules as Drools Rules
    participant ConfigStore as ConfigStore
    participant Adapter as DomainAdapter
    participant Result as UnderwriteResult

    Note over Client: API Request
    Client->>Controller: POST /decide/{domain}/{product}?clientId={clientId}
    Note over Client: Body: {"creditScore": 650, "income": 75000, ...}
    
    Controller->>Controller: Extract domain, product, clientId, payload
    Controller->>Service: decide(domain, product, clientId, payloadJson)
    
    Note over Service: Rule Execution Process
    Service->>Service: Parse JSON payload to JsonNode
    
    Service->>Adapter: adapter(domain) - Get domain-specific adapter
    Adapter-->>Service: DomainAdapter instance
    
    Service->>Adapter: normalize(product, clientId, node)
    Adapter->>Adapter: Transform JSON to FactsEnvelope
    Adapter-->>Service: FactsEnvelope with domain, attributes, derived data
    
    Service->>ConfigStore: load(domain, product, clientId)
    ConfigStore-->>Service: Configuration Map
    
    Service->>Service: env.setConfig(cfg) - Set configuration in envelope
    
    Service->>Registry: containerFor(domain)
    Registry-->>Service: KieContainer for domain
    
    Service->>KieContainer: newKieSession(sessionName)
    Note over Service: sessionName = domain + "-session" (e.g., "mortgage-session")
    KieContainer->>KieSession: Create new session
    KieSession-->>Service: KieSession instance
    
    Note over Service: Rule Execution Setup
    Service->>KieSession: setGlobal("config", env.getConfig())
    Service->>KieSession: insert(env) - Insert facts into session
    
    Service->>KieSession: getAgenda()
    KieSession-->>Service: Agenda instance
    
    Note over Service: Set agenda group priorities
    Service->>KieSession: getAgendaGroup("finalize").setFocus()
    Service->>KieSession: getAgendaGroup("risk").setFocus()
    Service->>KieSession: getAgendaGroup("affordability").setFocus()
    Service->>KieSession: getAgendaGroup("eligibility").setFocus()
    
    Note over Service: Execute Rules
    Service->>KieSession: fireAllRules()
    
    KieSession->>Rules: Evaluate rule conditions
    Note over Rules: Check domain == "mortgage", creditScore < 640, etc.
    
    alt Rule conditions met
        Rules->>Rules: Execute rule actions
        Rules->>Result: addReason("REJECT: Credit score below minimum")
        Rules->>Result: setStatus(Status.REJECTED)
    else No rules triggered
        Rules->>Result: setStatus(Status.APPROVED)
    end
    
    Rules-->>KieSession: Rules executed
    KieSession-->>Service: Rules execution completed
    
    Service->>KieSession: dispose() - Clean up session resources
    KieSession-->>Service: Session disposed
    
    Service->>Service: env.getResult() - Get final result
    Service-->>Controller: UnderwriteResult
    
    Controller->>Controller: Wrap in ResponseEntity
    Controller-->>Client: HTTP 200 OK with JSON response
    Note over Client: Response: {"status": "APPROVED", "reasons": []}
```

## Key Components

**Participants:**
- **API Client:** Initiates the request
- **DecisionController:** Receives the API request and orchestrates the decision process
- **EngineService:** Manages the rule execution, interacting with various rule engine components
- **RulesRegistry:** Provides access to KieContainers for specific domains
- **KieContainer:** A container for Drools knowledge bases
- **KieSession:** A runtime session for executing rules
- **Drools Rules:** The actual business rules being evaluated
- **ConfigStore:** Stores configuration data
- **DomainAdapter:** Transforms and normalizes data specific to a domain
- **UnderwriteResult:** Represents the outcome of the underwriting process

## Flow Overview

The process follows these main phases:

1. **API Request Processing:** Client sends request with domain, product, clientId, and payload
2. **Data Preparation:** Parse JSON, get domain adapter, normalize data, load configuration
3. **Session Setup:** Create KieSession, set global variables, insert facts, configure agenda groups
4. **Rule Execution:** Fire all rules with prioritized agenda groups (finalize → risk → affordability → eligibility)
5. **Result Processing:** Get final result, clean up session, return response

## Agenda Group Priority

The system uses prioritized agenda groups to ensure rules are executed in the correct order:
1. **finalize** - Final decision processing
2. **risk** - Risk assessment rules
3. **affordability** - Affordability calculation rules
4. **eligibility** - Basic eligibility rules

This ensures that more specific rules (like finalization) take precedence over general rules (like eligibility).
