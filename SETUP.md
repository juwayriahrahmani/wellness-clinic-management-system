# Wellness Clinic Management System - Setup Guide

This guide provides step-by-step instructions to set up and run the Wellness Clinic Management System.

## Prerequisites

### Backend Requirements
- **Java 8 or higher** (Java 11+ recommended)
- **PostgreSQL 12 or higher**
- **Maven 3.6+**
- **Eclipse IDE** (or IntelliJ IDEA)

### Frontend Requirements
- **Node.js 16+**
- **npm or yarn**

## Database Setup

### 1. Install PostgreSQL
```bash
# On Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# On macOS with Homebrew
brew install postgresql
brew services start postgresql

# On Windows
# Download and install from https://www.postgresql.org/download/windows/
```

### 2. Create Database and User
```bash
# Connect to PostgreSQL
sudo -u postgres psql

# Create database
CREATE DATABASE wellness_clinic;

# Create user (optional - you can use postgres user)
CREATE USER clinic_user WITH PASSWORD 'clinic_password';
GRANT ALL PRIVILEGES ON DATABASE wellness_clinic TO clinic_user;

# Exit
\q
```

### 3. Update Database Configuration
Update the following files with your database credentials:

**client-service/src/main/resources/application.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wellness_clinic
spring.datasource.username=postgres
spring.datasource.password=your_password
```

**appointment-service/src/main/resources/application.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wellness_clinic
spring.datasource.username=postgres
spring.datasource.password=your_password
```

## Backend Setup

### 1. Import Project into Eclipse
1. Open Eclipse IDE
2. Go to **File → Import → Existing Maven Projects**
3. Select the project root directory
4. Import both `client-service` and `appointment-service` modules

### 2. Build the Project
```bash
# From project root directory
mvn clean install
```

### 3. Run the Services

#### Option A: From Eclipse
1. Right-click on `ClientServiceApplication.java` → Run As → Java Application
2. Right-click on `AppointmentServiceApplication.java` → Run As → Java Application

#### Option B: From Command Line
```bash
# Terminal 1 - Client Service
cd client-service
mvn spring-boot:run

# Terminal 2 - Appointment Service
cd appointment-service
mvn spring-boot:run
```

Services will start on:
- **Client Service**: http://localhost:8081
- **Appointment Service**: http://localhost:8082

## Frontend Setup

### 1. Install Dependencies
```bash
cd frontend
npm install
```

### 2. Start the Development Server
```bash
npm start
```

The frontend will start on: http://localhost:3000

## Testing the Application

### 1. Test Backend APIs
You can test the APIs using curl or Postman:

```bash
# Test Client Service
curl http://localhost:8081/clients

# Test Appointment Service
curl http://localhost:8082/appointments

# Test creating an appointment
curl -X POST http://localhost:8082/appointments \
  -H "Content-Type: application/json" \
  -d '{"clientId":"1","time":"2024-12-31T10:00:00"}'
```

### 2. Test Frontend
1. Open http://localhost:3000 in your browser
2. You should see the Wellness Clinic Admin interface
3. Test the following features:
   - View client list
   - Search clients
   - View upcoming appointments
   - Schedule new appointments

## Mock API Configuration

Since the external API URLs are mock endpoints, you can:

1. **Use the built-in mock data**: The services will work with local data
2. **Set up a mock server**: Use tools like:
   - **json-server**: `npm install -g json-server`
   - **Postman Mock Server**
   - **WireMock**

### Example mock server setup:
```bash
# Install json-server
npm install -g json-server

# Create mock data file
cat > mock-data.json << EOF
{
  "clients": [
    {"id": "1", "name": "John Doe", "email": "john@example.com", "phone": "1234567890"},
    {"id": "2", "name": "Jane Smith", "email": "jane@example.com", "phone": "9876543210"}
  ],
  "appointments": [
    {"id": "a1", "client_id": "1", "time": "2024-12-31T10:00:00Z"},
    {"id": "a2", "client_id": "2", "time": "2024-12-31T11:00:00Z"}
  ]
}
EOF

# Start mock server
json-server --watch mock-data.json --port 3001
```

Then update the application.properties files:
```properties
external.api.url.clients=http://localhost:3001/clients
external.api.url.appointments=http://localhost:3001/appointments
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
- Ensure PostgreSQL is running: `sudo systemctl status postgresql`
- Check if the database exists: `\l` in psql
- Verify credentials in application.properties

#### 2. Port Conflicts
- Change ports in application.properties:
  ```properties
  server.port=8083  # For client service
  server.port=8084  # For appointment service
  ```

#### 3. CORS Issues
- Ensure CORS is properly configured in both services
- Check if frontend is running on the allowed origin

#### 4. Java Version Issues
- Ensure Java 8+ is installed: `java -version`
- Set JAVA_HOME if needed:
  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
  ```

### Debug Mode
Enable debug logging:
```properties
logging.level.org.springframework.web=DEBUG
logging.level.com.clinic=DEBUG
```

## Production Deployment

### 1. Build Frontend
```bash
cd frontend
npm run build
```

### 2. Build Backend JARs
```bash
# From project root
mvn clean package
```

### 3. Run JARs
```bash
# Client Service
java -jar client-service/target/client-service-1.0.0.jar

# Appointment Service
java -jar appointment-service/target/appointment-service-1.0.0.jar
```

### 4. Docker Deployment (Optional)
Create Docker containers for each service (see docker-compose.yml in future updates).

## API Documentation

### Client Service Endpoints
- `GET /clients` - Get all clients
- `GET /clients/{id}` - Get client by ID
- `GET /clients/search?name={name}` - Search clients by name
- `POST /clients` - Create new client
- `PUT /clients/{id}` - Update client
- `DELETE /clients/{id}` - Delete client
- `POST /clients/sync` - Manual sync with external API

### Appointment Service Endpoints
- `GET /appointments` - Get all appointments
- `GET /appointments/upcoming` - Get upcoming appointments
- `GET /appointments/today` - Get today's appointments
- `GET /appointments/{id}` - Get appointment by ID
- `GET /appointments/client/{clientId}` - Get appointments for client
- `POST /appointments` - Create new appointment
- `PUT /appointments/{id}` - Update appointment
- `PATCH /appointments/{id}/cancel` - Cancel appointment
- `DELETE /appointments/{id}` - Delete appointment
- `POST /appointments/sync` - Manual sync with external API

## Support
For issues or questions:
1. Check the logs in the console
2. Verify all services are running
3. Ensure database is accessible
4. Check network connectivity between services
