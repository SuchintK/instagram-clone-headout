# Instagram Clone

A Spring Boot application that clones the core functionality of Instagram.

## Project Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 5.7 or higher

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Test Endpoints

- **Home**: `http://localhost:8080/`
- **Health Check**: `http://localhost:8080/api/health`

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/instagram/
│   │       ├── App.java                 (Spring Boot entry point)
│   │       └── controller/
│   │           └── HelloController.java (Sample REST controller)
│   └── resources/
│       └── application.properties        (Configuration file)
└── test/
    └── java/
        └── com/instagram/
            └── AppTest.java
```

## Dependencies

- Spring Boot Web Starter
- Spring Boot Data JPA
- Spring Boot Security
- Spring Boot Validation
- MySQL Connector
- Lombok
- Spring Boot DevTools

## Database Configuration

Update `application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/instagram_clone
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Next Steps

1. Create entity classes for Users, Posts, Comments, etc.
2. Create repository interfaces extending JpaRepository
3. Create service classes for business logic
4. Build REST controllers for API endpoints
5. Implement authentication and authorization

## License

MIT License
