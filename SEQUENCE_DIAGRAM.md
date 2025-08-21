# Underwriter Rules Engine - Sequence Diagrams

## 1. Application Startup & Rules Onboarding Flow

```mermaid
sequenceDiagram
    participant App as Spring Boot App
    participant Registry as RulesRegistry
    participant KieServices as KieServices
    participant MavenRepo as Maven Repository
    participant Classpath as Classpath Container
    participant RuleModules as Rule Modules (JARs)
    participant KieContainer as KieContainer
    participant KieBase as KieBase
    participant KieSession as KieSession

    Note over App: Application Startup
    App->>Registry: Initialize RulesRegistry()
    
    Note over Registry: Dynamic Loading Strategy
    Registry->>KieServices: getKieServices()
    KieServices-->>Registry: KieServices instance
    
    Note over Registry: Try Dynamic Loading First
    Registry->>Registry: tryDynamicLoading()
    
    loop For each domain (mortgage, travel, auto, life)
        Registry->>KieServices: newReleaseId(groupId, artifactId, version)
        KieServices-->>Registry: ReleaseId
        
        Registry->>KieServices: newKieContainer(ReleaseId)
        KieServices->>MavenRepo: Fetch rule module from repository
        alt Dynamic Loading Successful
            MavenRepo-->>KieServices: Rule module JAR
            KieServices->>KieContainer: Create KieContainer
            KieContainer->>KieBase: Load KieBase from JAR
            KieBase->>KieSession: Create KieSession
            KieContainer-->>Registry: KieContainer instance
            Registry->>Registry: Store KieContainer for domain
        else Dynamic Loading Failed
            MavenRepo-->>KieServices: Error (not found/invalid)
            KieServices-->>Registry: Exception
        end
    end
    
    alt Dynamic Loading Failed for any domain
        Note over Registry: Fallback to Classpath Loading
        Registry->>Registry: loadFromClasspath()
        Registry->>KieServices: getKieClasspathContainer()
        KieServices->>Classpath: Scan classpath for rule modules
        Classpath->>RuleModules: Load embedded rule files
        RuleModules-->>Classpath: Rule definitions
        Classpath-->>KieServices: KieContainer with all rules
        KieServices-->>Registry: KieContainer instance
        
        loop For each domain
            Registry->>Registry: Store same KieContainer for all domains
        end
    end
    
    Registry-->>App: RulesRegistry initialized
    App->>App: Start HTTP server on port 8081
    Note over App: Application ready to handle requests
```

## 2. Request/Response Flow for Rule Execution

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

## 3. Rules Development & Deployment Flow

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant RuleModule as Rule Module Project
    participant Maven as Maven Build
    participant LocalRepo as Local Maven Repository
    participant RemoteRepo as Remote Maven Repository
    participant App as Application
    participant Registry as RulesRegistry

    Note over Dev: Rules Development Process
    
    Dev->>RuleModule: Create new rule module (e.g., rules-health)
    Dev->>RuleModule: Add pom.xml with kie-maven-plugin
    Dev->>RuleModule: Create kmodule.xml with kbase/ksession
    Dev->>RuleModule: Write DRL rules in main.drl
    
    Note over Dev: Build and Test
    Dev->>Maven: mvn clean compile
    Maven->>RuleModule: Compile rules
    RuleModule-->>Maven: Compiled rule classes
    Maven-->>Dev: Build successful
    
    Dev->>Maven: mvn test
    Maven->>RuleModule: Run rule tests
    RuleModule-->>Maven: Test results
    Maven-->>Dev: Tests passed
    
    Note over Dev: Deploy to Repository
    Dev->>Maven: mvn install
    Maven->>LocalRepo: Install to local repository
    LocalRepo-->>Maven: Installation successful
    Maven-->>Dev: Installed locally
    
    Dev->>Maven: mvn deploy
    Maven->>RemoteRepo: Deploy to remote repository
    RemoteRepo-->>Maven: Deployment successful
    Maven-->>Dev: Deployed to remote
    
    Note over Dev: Register in Application
    Dev->>App: Add dependency to underwriter-app/pom.xml
    Dev->>App: Register domain in RulesRegistry.java
    
    Note over App: Application Deployment
    App->>App: Build with new rule dependency
    App->>Registry: Initialize with new domain
    
    Registry->>Registry: tryDynamicLoading()
    Registry->>RemoteRepo: Fetch new rule module
    RemoteRepo-->>Registry: New rule module
    Registry->>Registry: Create KieContainer for new domain
    Registry-->>App: New domain registered
    
    Note over App: Application ready with new rules
    App->>App: Start application
    Note over App: New domain rules available via API
```

## 4. Error Handling & Fallback Flow

```mermaid
sequenceDiagram
    participant App as Application
    participant Registry as RulesRegistry
    participant KieServices as KieServices
    participant MavenRepo as Maven Repository
    participant Classpath as Classpath Container
    participant Service as EngineService
    participant Client as API Client

    Note over App: Application Startup with Error Handling
    
    App->>Registry: Initialize RulesRegistry()
    Registry->>Registry: tryDynamicLoading()
    
    Registry->>KieServices: newKieContainer(ReleaseId)
    KieServices->>MavenRepo: Fetch rule module
    
    alt Maven Repository Unavailable
        MavenRepo-->>KieServices: Connection timeout/error
        KieServices-->>Registry: Exception
        Registry->>Registry: Dynamic loading failed
    else Rule Module Not Found
        MavenRepo-->>KieServices: 404 Not Found
        KieServices-->>Registry: Exception
        Registry->>Registry: Dynamic loading failed
    else Invalid Rule Module
        MavenRepo-->>KieServices: Corrupted JAR/XML
        KieServices-->>Registry: Exception
        Registry->>Registry: Dynamic loading failed
    end
    
    Note over Registry: Fallback to Classpath Loading
    Registry->>Registry: loadFromClasspath()
    Registry->>KieServices: getKieClasspathContainer()
    KieServices->>Classpath: Load embedded rules
    Classpath-->>KieServices: KieContainer with embedded rules
    KieServices-->>Registry: KieContainer instance
    Registry->>Registry: Store KieContainer for all domains
    Registry-->>App: RulesRegistry initialized with fallback
    
    Note over App: Application ready with fallback rules
    
    Note over Client: API Request Processing
    Client->>App: POST /decide/mortgage/standard
    App->>Service: Process request
    
    Service->>Registry: containerFor("mortgage")
    Registry-->>Service: KieContainer (from classpath)
    
    Service->>Service: Execute rules with fallback container
    Service-->>App: UnderwriteResult
    App-->>Client: HTTP 200 with result
    
    Note over App: Application continues to work with embedded rules
```

## 5. Rule Update & Hot Reload Flow

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant RuleModule as Rule Module
    participant Maven as Maven Build
    participant RemoteRepo as Remote Repository
    participant KieScanner as KieScanner
    participant KieContainer as KieContainer
    participant App as Application
    participant Client as API Client

    Note over Dev: Rule Update Process
    
    Dev->>RuleModule: Modify rule logic in main.drl
    Dev->>RuleModule: Update rule version in pom.xml
    
    Dev->>Maven: mvn clean install deploy
    Maven->>RemoteRepo: Deploy updated rule module
    RemoteRepo-->>Maven: Deployment successful
    Maven-->>Dev: New version deployed
    
    Note over App: Hot Reload Process (if KieScanner enabled)
    
    alt KieScanner Enabled
        KieScanner->>RemoteRepo: Check for new versions
        RemoteRepo-->>KieScanner: New version available
        KieScanner->>KieContainer: Update KieContainer
        KieContainer->>KieContainer: Reload rules
        KieContainer-->>KieScanner: Updated KieContainer
        KieScanner-->>App: Rules updated automatically
    else Manual Restart Required
        App->>App: Restart application
        App->>App: Load new rule versions on startup
    end
    
    Note over Client: Test Updated Rules
    Client->>App: POST /decide/mortgage/standard
    App->>App: Execute with updated rules
    App-->>Client: Response with new rule logic
    
    Note over App: Application now using updated rules
```

## Key Benefits of This Architecture

1. **Scalability**: New domains can be added without modifying existing code
2. **Reliability**: Fallback mechanisms ensure application always works
3. **Flexibility**: Support for both dynamic and embedded rule loading
4. **Maintainability**: Clear separation between rule logic and application logic
5. **Hot Reloading**: Rules can be updated without application restarts (with KieScanner)
6. **Domain Isolation**: Each domain's rules are independent and can be versioned separately
