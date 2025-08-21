# Application Startup & Rules Onboarding Flow

This sequence diagram illustrates the application startup process, specifically focusing on how rule modules are loaded and initialized within a Spring Boot application.

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

## Key Components

**Participants:**
- **Spring Boot App:** The main application initiating the process
- **RulesRegistry:** Manages and registers rules
- **KieServices:** Service for managing Kie (Knowledge Is Everything) framework
- **Maven Repository:** External repository for fetching rule modules (JARs)
- **Classpath Container:** Scans application's classpath for rule modules
- **Rule Modules (JARs):** Actual rule definitions packaged as JAR files
- **KieContainer:** Container for KieBases and KieSessions
- **KieBase:** Repository of all knowledge definitions
- **KieSession:** Active session where rules are executed

## Flow Overview

The diagram shows two main strategies for loading rules:
1. **Dynamic Loading Strategy (Preferred):** Attempts to fetch rule modules from a Maven Repository
2. **Fallback to Classpath Loading:** If dynamic loading fails, loads rules from the application's classpath

This architecture ensures the application can start successfully even if external repositories are unavailable, providing both flexibility and reliability.
