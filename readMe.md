# Brokerage Management API

## Overview

The Brokerage Management API is a robust, scalable solution for managing financial transactions in a brokerage environment. This Spring Boot-based application provides a secure and efficient platform for handling orders, assets, and user accounts in the financial market.

## v2 branch is available (15th october)

# Postman Collection

To help you quickly test and explore our API, we've provided a Postman collection that includes pre-configured requests for all our endpoints.

### Accessing the Postman Collection

You can find our Postman collection [here](https://github.com/ugurarabaci/BrokerageManagementAPI/blob/master/BrokerageManagementAPI.postman_collection.json).


## Key Features

- **Authentication**: Secure user authentication and authorization system.
- **Order Management**: Create, update, and match buy/sell orders.
- **Asset Tracking**: Real-time tracking of user assets and balances.
- **Rate Limiting**: Protect the API from abuse with built-in rate limiting.
- **Comprehensive Testing**: Extensive unit and integration tests ensuring reliability.

## Technology Stack

- Java 17
- Spring Boot 2.7.x
- Spring Security
- Spring Data JPA
- H2 Database
- JUnit 5 & Mockito
- Maven

## Getting Started

### Prerequisites

- JDK 17
- Maven 3.6+

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/ugurarabaci/BrokerageManagementAPI.git
   
   ```

2. Navigate to the project directory:
   ```
   cd brokerage-management-api
   ```

3. Configure the database connection in `src/main/resources/application.properties`.

4. Build the project:
   ```
   mvn clean install
   ```

5. Run the application:
   ```
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

## API Documentation

Detailed API documentation is available via Swagger UI at `http://localhost:8080/swagger-ui.html` when the application is running.

### Key Endpoints

- `/api/orders`: Manage orders (create, update, delete)
- `/api/orders/match`: Match pending orders
- `/api/assets`: View and manage user assets

## Security

The API uses in memory Authorization and Authentication. The default credentials are:
You can check credentials at SecurityConfig.java

## Rate Limiting

The API implements rate limiting to prevent abuse. The current limit is set to 5 requests per minute for the order matching endpoint.

## Support

For support, please open an issue in the GitHub issue tracker or contact me ugurarabaci0209@gmail.com.

## Dockerization

Our application can be dockerized using the following Dockerfile:

[here](https://github.com/ugurarabaci/BrokerageManagementAPI/blob/master/Dockerfile)

Note: login to http://localhost:8088/h2-console with username: sa, password: password infos (which already defined in application properties)
