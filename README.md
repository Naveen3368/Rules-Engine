# Underwriter Rules Engine

A sophisticated, domain-driven rules engine built with Spring Boot and Drools, designed for financial underwriting decisions across multiple domains (Mortgage, Auto, Life, and Travel insurance).

## 🚀 Features

- **Multi-Domain Support**: Handles underwriting for Mortgage, Auto, Life, and Travel insurance
- **Dynamic Rule Loading**: Supports both dynamic loading from Maven repositories and fallback to embedded rules
- **Configurable Rules**: Domain-specific configurations for different clients and products
- **RESTful API**: Clean REST endpoints for decision requests
- **Docker Support**: Containerized deployment with Docker and Docker Compose
- **Comprehensive Documentation**: Detailed sequence diagrams and architecture documentation

## 🏗️ Architecture

The system follows a modular architecture with clear separation of concerns:

- **Domain Adapters**: Transform and normalize data for each insurance domain
- **Rules Registry**: Manages rule loading and KieContainer lifecycle
- **Engine Service**: Orchestrates rule execution and decision processing
- **Configuration Store**: Manages domain-specific configurations
- **REST Controller**: Handles HTTP requests and responses

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
