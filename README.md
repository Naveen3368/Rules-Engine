# Underwriter Rules Engine - Scalable Modular Architecture

## Overview

This is a scalable, modular rules engine built with Spring Boot and Drools that supports dynamic rule loading and domain-specific rule management.

## Architecture

### Core Components

1. **Main Application** (`underwriter-app`)
   - Spring Boot web application
   - REST API endpoints for rule execution
   - Dynamic rule loading and management

2. **Rule Modules** (Separate Maven projects)
   - `rules-mortgage` - Mortgage underwriting rules
   - `rules-travel` - Travel insurance rules
   - `rules-auto` - Auto insurance rules
   - `rules-life` - Life insurance rules

3. **Dynamic Rule Loading**
   - Attempts to load rules dynamically from Maven repository
   - Falls back to classpath loading if dynamic loading fails
   - Supports hot-reloading of rule updates

## Scalable Design Principles

### 1. Modular Rule Development
Each domain has its own rule module:
```
rules-mortgage/
├── src/main/resources/
│   ├── META-INF/kmodule.xml
│   └── rules/main.drl
└── pom.xml

rules-travel/
├── src/main/resources/
│   ├── META-INF/kmodule.xml
│   └── rules/main.drl
└── pom.xml
```

### 2. Dynamic Rule Loading
The `RulesRegistry` implements a two-tier loading strategy:

```java
private void initializeContainers() {
    try {
        // First, try to load rules dynamically from Maven repository
        if (tryDynamicLoading()) {
            return;
        }
        
        // Fallback to classpath loading if dynamic loading fails
        loadFromClasspath();
        
    } catch (Exception e) {
        throw new RuntimeException("Failed to initialize rule containers", e);
    }
}
```

### 3. Domain-Specific Rule Management
Each domain has its own KieContainer and session:
- `mortgage-session` for mortgage rules
- `travel-session` for travel rules
- `auto-session` for auto rules
- `life-session` for life rules

## Adding New Rule Domains

### Step 1: Create New Rule Module
```bash
# Create new rule module
mkdir rules-health
cd rules-health
```

### Step 2: Configure Module
Create `pom.xml`:
```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>rules-health</artifactId>
    <version>0.2.1</version>
    <packaging>jar</packaging>
    
    <dependencies>
        <dependency>
            <groupId>org.kie</groupId>
            <artifactId>kie-api</artifactId>
            <version>${drools.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.kie</groupId>
                <artifactId>kie-maven-plugin</artifactId>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 3: Create Rules
Create `src/main/resources/META-INF/kmodule.xml`:
```xml
<kmodule xmlns="http://www.drools.org/xsd/kmodule">
  <kbase name="health-rules" packages="rules">
    <ksession name="health-session"/>
  </kbase>
</kmodule>
```

Create `src/main/resources/rules/main.drl`:
```drl
package rules

import com.example.underwriter.model.FactsEnvelope;
import com.example.underwriter.model.UnderwriteResult.Status;
import java.util.Map;

global java.util.Map config;

rule "health.age.reject"
  agenda-group "eligibility"
  salience 100
when
  $env : FactsEnvelope( domain == "health" )
  $age : Integer() from ((Map)$env.getAttributes()).get("age")
  eval( $age != null && $age > 65 )
then
  $env.getResult().addReason("REJECT: Age exceeds maximum");
end

rule "health.finalize"
  agenda-group "finalize"
  salience 10
when
  $env : FactsEnvelope( domain == "health" )
then
  $env.getResult().setStatus(Status.APPROVED);
end
```

### Step 4: Register in Main Application
Add to `RulesRegistry.java`:
```java
// In tryDynamicLoading() method
loadDomainRules("health", "com.example", "rules-health", "0.2.1");

// In loadFromClasspath() method
containers.put("health", kc);
```

### Step 5: Add Dependency
Add to `underwriter-app/pom.xml`:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>rules-health</artifactId>
    <version>${project.version}</version>
</dependency>
```

## API Usage

### Endpoint
```
POST /decide/{domain}/{product}?clientId={clientId}
```

### Examples

**Mortgage Decision:**
```bash
curl -X POST "http://localhost:8081/decide/mortgage/standard?clientId=test123" \
  -H "Content-Type: application/json" \
  -d '{
    "creditScore": 650,
    "income": 75000,
    "loanAmount": 300000,
    "propertyValue": 350000
  }'
```

**Health Insurance Decision:**
```bash
curl -X POST "http://localhost:8081/decide/health/standard?clientId=test123" \
  -H "Content-Type: application/json" \
  -d '{
    "age": 45,
    "medicalHistory": "clean",
    "coverageAmount": 500000
  }'
```

## Benefits of This Architecture

### 1. **Scalability**
- New domains can be added without modifying existing code
- Each domain is isolated and can be developed independently
- Rules can be updated independently per domain

### 2. **Maintainability**
- Clear separation of concerns
- Domain-specific rule logic
- Easy to test individual rule modules

### 3. **Flexibility**
- Dynamic rule loading from Maven repository
- Fallback mechanisms for reliability
- Support for hot-reloading rule updates

### 4. **Deployment Options**
- **Development**: Use classpath loading for quick iteration
- **Production**: Use dynamic loading for rule updates without application restarts
- **Hybrid**: Combine both approaches for maximum flexibility

## Running the Application

### Development Mode
```bash
docker compose up --build
```

### Production Mode
```bash
# Build and deploy rule modules to Maven repository
mvn clean install deploy

# Run application with dynamic rule loading
docker compose up --build
```

## Monitoring and Debugging

### Check Rule Loading
```bash
docker compose logs | grep "Successfully loaded"
```

### Test Rule Execution
```bash
# Test approval case
curl -X POST "http://localhost:8081/decide/mortgage/standard?clientId=test123" \
  -H "Content-Type: application/json" \
  -d '{"creditScore":650,"income":75000,"loanAmount":300000,"propertyValue":350000}'

# Test rejection case
curl -X POST "http://localhost:8081/decide/mortgage/standard?clientId=test123" \
  -H "Content-Type: application/json" \
  -d '{"creditScore":600,"income":75000,"loanAmount":300000,"propertyValue":350000}'
```

## Future Enhancements

1. **Rule Versioning**: Support multiple rule versions per domain
2. **Rule Validation**: Add rule validation and testing frameworks
3. **Performance Monitoring**: Add metrics for rule execution performance
4. **Rule Templates**: Create reusable rule templates for common patterns
5. **Web UI**: Add web interface for rule management and testing
