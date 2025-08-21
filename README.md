# Underwriter Rules Engine

A sophisticated, domain-driven rules engine built with Spring Boot and Drools, designed for financial underwriting decisions across multiple domains (Mortgage, Auto, Life, and Travel insurance).

## 🚀 Features

- **Multi-Domain Support**: Handles underwriting for Mortgage, Auto, Life, and Travel insurance
- **Dynamic Rule Loading**: Supports both dynamic loading from Maven repositories and fallback to embedded rules
- **Configurable Rules**: Domain-specific configurations for different clients and products
- **RESTful API**: Clean REST endpoints for decision requests
- **Docker Support**: Containerized deployment with Docker and Docker Compose
- **Comprehensive Documentation**: Detailed sequence diagrams and architecture documentation
- **Dynamic Rule Configuration**: Flexible rule validation with optional/mandatory fields and client-specific thresholds
- **KJAR Support**: Knowledge JAR (KJAR) integration with KieScanner for hot deployment

## 🏗️ Architecture

The system follows a modular architecture with clear separation of concerns:

- **Domain Adapters**: Transform and normalize data for each insurance domain
- **Rules Registry**: Manages rule loading and KieContainer lifecycle
- **Engine Service**: Orchestrates rule execution and decision processing
- **Configuration Store**: Manages domain-specific configurations
- **REST Controller**: Handles HTTP requests and responses
- **Dynamic Rule Engine**: Configurable validation rules with client-specific thresholds

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker (optional, for containerized deployment)

## 🛠️ Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Naveen3368/Rules-Engine.git
cd Rules-Engine
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

#### Option A: Direct Execution
```bash
cd underwriter-app
mvn spring-boot:run
```

#### Option B: Docker Deployment
```bash
docker-compose up -d
```

The application will start on port 8081.

## 🎯 Usage

### API Endpoints

The application exposes a REST API for underwriting decisions:

```
POST /decide/{domain}/{product}?clientId={clientId}
```

### Supported Domains
- `mortgage` - Mortgage insurance underwriting
- `auto` - Auto insurance underwriting  
- `life` - Life insurance underwriting
- `travel` - Travel insurance underwriting

### Example Requests

#### Mortgage Underwriting
```bash
curl -X POST "http://localhost:8081/decide/mortgage/standard?clientId=default" \
  -H "Content-Type: application/json" \
  -d '{
    "creditScore": 750,
    "income": 85000,
    "downPayment": 50000,
    "loanAmount": 200000,
    "propertyValue": 250000
  }'
```

#### Auto Insurance
```bash
curl -X POST "http://localhost:8081/decide/auto/standard?clientId=default" \
  -H "Content-Type: application/json" \
  -d '{
    "driverAge": 25,
    "drivingHistory": 5,
    "vehicleValue": 25000,
    "coverageType": "comprehensive"
  }'
```

### Sample Responses

#### Approved Application
```json
{
  "status": "APPROVED",
  "reasons": [],
  "score": 85,
  "premium": 1200.00
}
```

#### Rejected Application
```json
{
  "status": "REJECTED",
  "reasons": [
    "Credit score below minimum threshold",
    "Income insufficient for loan amount"
  ],
  "score": 45
}
```

## 📁 Project Structure

```
├── rules-auto/                 # Auto insurance rules module
├── rules-life/                 # Life insurance rules module
├── rules-mortgage/             # Mortgage insurance rules module
├── rules-travel/               # Travel insurance rules module
├── underwriter-app/            # Main Spring Boot application
│   ├── src/main/java/
│   │   └── com/example/underwriter/
│   │       ├── adapter/        # Domain-specific adapters
│   │       ├── config/         # Configuration classes
│   │       ├── controller/     # REST controllers
│   │       ├── model/          # Data models
│   │       └── service/        # Business logic services
│   ├── src/main/resources/
│   │   ├── config/             # Domain configurations
│   │   └── samples/            # Sample request/response files
│   └── pom.xml
├── docker-compose.yml          # Docker Compose configuration
├── Dockerfile                  # Docker image definition
└── pom.xml                     # Parent POM
```

## 🔧 Configuration

### Domain Configurations

Each domain has its own configuration files located in `underwriter-app/src/main/resources/config/{domain}/`:

- `default.json` - Default configuration for the domain
- `clientB.json` - Client-specific configuration

### Rule Modules

Each domain has its own rule module with:
- `pom.xml` - Maven configuration
- `src/main/resources/META-INF/kmodule.xml` - Drools module configuration
- `src/main/resources/rules/main.drl` - Business rules

## 📊 Sequence Diagrams

The project includes detailed sequence diagrams showing:

1. **[Application Startup Flow](application-startup-sequence.md)** - How rule modules are loaded during application startup
2. **[Rule Execution Flow](rule-execution-sequence.md)** - How decisions are processed from API request to response

## 🐳 Docker Deployment

### Build and Run with Docker

```bash
# Build the application
docker build -t underwriter-rules-engine .

# Run the container
docker run -p 8081:8081 underwriter-rules-engine
```

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 🧪 Testing

### Sample Data

The project includes sample JSON files for testing each domain:

- `underwriter-app/samples/mortgage-default-approve.json`
- `underwriter-app/samples/auto-clientB-reject.json`
- `underwriter-app/samples/life-default-approve.json`
- `underwriter-app/samples/travel-clientB-reject.json`

### Running Tests

```bash
# Run all tests
mvn test

# Run specific module tests
cd rules-mortgage && mvn test
```

## 🔄 Adding New Domains

To add a new domain:

1. Create a new rule module (e.g., `rules-health/`)
2. Add domain adapter in `underwriter-app/src/main/java/com/example/underwriter/adapter/`
3. Add configuration files in `underwriter-app/src/main/resources/config/`
4. Register the domain in `RulesRegistry.java`
5. Add sample data in `underwriter-app/samples/`

## 🤖 GPT Prompts & Dynamic Rule Configuration

### 1. Dynamic Rule Configuration

The rules engine supports dynamic configuration where rules can be made optional or mandatory based on business requirements:

#### Configuration Structure
```json
{
  "fieldValidations": {
    "age": {
      "mandatory": false,
      "thresholds": {
        "clientA": 18,
        "clientB": 20,
        "default": 18
      }
    },
    "creditScore": {
      "mandatory": true,
      "thresholds": {
        "clientA": 700,
        "clientB": 750,
        "default": 650
      }
    },
    "employmentYears": {
      "mandatory": false,
      "thresholds": {
        "clientA": 2,
        "clientB": 3,
        "default": 1
      }
    }
  }
}
```

#### Rule Generation Logic
```java
// Dynamic rule generation based on configuration
if (config.isFieldMandatory(fieldName) || request.hasField(fieldName)) {
    int threshold = config.getThreshold(fieldName, clientId);
    // Generate validation rule with threshold
}
```

### 2. Multi-Domain Support

The system supports multiple insurance domains with different rule sets:

#### Domain-Specific Adapters
- **Mortgage Adapter**: Handles credit score, income, down payment, loan amount
- **Travel Adapter**: Manages trip duration, destination risk, traveler age
- **Auto Adapter**: Processes driver age, driving history, vehicle value
- **Life Adapter**: Evaluates age, health conditions, coverage amount

#### Domain Configuration
```json
{
  "mortgage": {
    "rules": ["creditScore", "income", "downPayment", "loanAmount"],
    "mandatory": ["creditScore", "income"]
  },
  "travel": {
    "rules": ["tripDuration", "destinationRisk", "travelerAge"],
    "mandatory": ["tripDuration", "travelerAge"]
  }
}
```

### 3. Working Application Features

#### Supported Domains
- **Mortgage Insurance**: Complete underwriting with configurable thresholds
- **Travel Insurance**: Trip-based risk assessment
- **Auto Insurance**: Vehicle and driver risk evaluation
- **Life Insurance**: Health and age-based assessment

#### Docker Compose Setup
```yaml
version: '3.8'
services:
  underwriter-app:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    volumes:
      - ./config:/app/config
```

#### Sample Test Data

**Mortgage Approval (Client A)**
```json
{
  "domain": "mortgage",
  "product": "standard",
  "clientId": "clientA",
  "data": {
    "creditScore": 750,
    "income": 85000,
    "downPayment": 50000,
    "loanAmount": 200000,
    "propertyValue": 250000
  }
}
```

**Travel Insurance (Client B)**
```json
{
  "domain": "travel",
  "product": "premium",
  "clientId": "clientB",
  "data": {
    "tripDuration": 14,
    "destinationRisk": "low",
    "travelerAge": 35,
    "coverageAmount": 50000
  }
}
```

### 4. KJAR & KieScanner Capabilities

#### What is KJAR?
**KJAR (Knowledge JAR)** is a Maven artifact that contains Drools rules, processes, and other knowledge assets. It enables:
- **Versioned Rules**: Rules are versioned and managed like code
- **Hot Deployment**: Rules can be updated without restarting the application
- **Rule Isolation**: Each domain has its own rule set
- **Maven Integration**: Rules are built and deployed using Maven

#### KieScanner Benefits
```java
// Automatic rule scanning and reloading
KieScanner kieScanner = kieServices.newKieScanner(kieContainer);
kieScanner.start(10000L); // Scan every 10 seconds
```

#### Capabilities
- **Dynamic Rule Loading**: Load rules from Maven repositories
- **Version Management**: Support multiple rule versions
- **Hot Deployment**: Update rules without application restart
- **Fallback Support**: Embedded rules when external KJAR unavailable

#### Rule Module Structure
```
rules-mortgage/
├── pom.xml
├── src/main/resources/
│   ├── META-INF/kmodule.xml
│   └── rules/
│       ├── main.drl
│       ├── credit-rules.drl
│       └── income-rules.drl
```

#### Sample Rules
```drools
// Dynamic credit score validation
rule "Credit Score Validation"
when
    $request: MortgageRequest(creditScore < $threshold)
    $config: DomainConfig(clientId == $request.clientId)
    $field: FieldConfig(name == "creditScore", mandatory == true)
then
    insert(new ValidationError("Credit score below threshold"));
end
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Naveen** - [GitHub Profile](https://github.com/Naveen3368)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Drools team for the powerful rules engine
- Apache Maven for build management
- Docker team for containerization support

---

⭐ **Star this repository if you find it helpful!**
GPT Prompts

1)Rewrite this prompt to support below mentioned rules in drools. The rules that i gave are hardcoded, want to make my rules in a dynamic way. for example age is check is optional, credit score is mandatory, employment year is optional, ect.., If value is provided for any field ( even if its optional we need to perform validation with different threshold limits, for eg for client age limit is 18, other client age limit is 20, how can we build a rules engine with configuration to support above requirement)
2)This is great but it will  fail if we apply another business domain for example  life insurance, travel insurance, car insurance domain which has different rules with different attributes then how can you  solve it.  
3)i want it have working application to support mortgage and travel insurance underwriter capabilities, please provide complete code base with docker compose so that i can run locally in addition to that give me couple sample  test data to test mortgage underwriter and travel underwriter with different configured threshold limits which should support based on all above prompts. 
4)I have no idea on KJAR + KieScanner what is this and what are the capabilities how does it help in this situation. of course i need to extend for auto and life. Please give me update code with various possible test rules. 
