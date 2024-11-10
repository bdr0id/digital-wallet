# Digital Wallet

A Spring Boot-based digital wallet service that offers users a range of financial operations and account management capabilities. This application provides essential features, such as user creation, a simulated KYC check, wallet creation, and various wallet-related functionalities for top-ups, withdrawals, and transfers.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Setup and Installation](#setup-and-installation)
5. [Running the Application](#running-the-application)
6. [Configuration](#configuration)
7. [API Endpoints](#api-endpoints)
8. [Built With](#built-with)

---

## Project Overview

The Digital Wallet allows users to:
1. Register and create a wallet.
2. Simulate a KYC check.
3. Perform various wallet operations such as loading funds, transferring money, and managing account settings.

---

## Features

The wallet service includes the following features:

### User Creation
- Allows users to register and create an account in the digital wallet system.

### Fake KYC Check
- Performs a simulated KYC (Know Your Customer) verification to verify the user.

### Wallet Operations
1. **Top-Up**:
    - Users can load money into their wallets via M-Pesa (simulated).

2. **Withdrawals and Transfers (P2P)**
    - Users can transfer funds from their wallets to other wallets, enabling peer-to-peer (P2P) transactions.

3. **Account Management**
    - Users have control over their wallet accounts with the ability to:
        - Request account statements.
        - Generate a QR code for easy wallet identification.
        - Set up and change a secure wallet PIN.
        - Enable or disable the wallet.
        - Permanently delete the wallet if no longer needed.

---

## Prerequisites

Before you start, ensure you have the following installed:
- [Java 21+](https://www.oracle.com/java/technologies/downloads/)
- [Maven](https://maven.apache.org/install.html)
- [MySQL](https://dev.mysql.com/downloads/) or another compatible database

---

## Setup and Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/l00pinfinity/wallet.git
   cd wallet
   ```

2. **Configure Database**
    - Create a MySQL database for development (e.g., `wallet`).
    - Update the `application.yml` file with your database configuration.

3. **Install Dependencies**
   ```bash
   mvn clean install
   ```

---

## Running the Application

1. **Start the Application**
   Run the application using the following command:
   ```bash
   mvn spring-boot:run
   ```

2. **Access the API**
    - The application will start on `http://localhost:5000`.
    - Use Postman or another API testing tool to interact with the endpoints.

---

## Configuration

This project uses `application.yml` for configuration. Profile-specific configurations allow for environment-specific settings (development, production, etc.):

```yaml
# application.yml
spring:
  profiles:
    active: dev # Change to 'prod' for production
  datasource:
    url: jdbc:mysql://localhost:3306/wallet-dev
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

You can add separate configurations for `prod` and `dev` profiles within the same `application.yml` file to manage different database and Hibernate settings.

---

## API Endpoints

### User Management
- **POST** `/api/v1/users/create` - Register a new user.

### KYC Check
- **POST** `/api/v1/users/{id}/kyc-check` - Simulate a KYC verification.

### Wallet Operations
1. **Top-Up**
    - **POST** `/api/v1/wallets/{id}/top-up` - Load funds into the user’s wallet.

2. **Withdrawals and Transfers**
    - **POST** `/api/v1/wallets/{id}/transfer` - Transfer funds to another wallet.

3. **Account Management**
    - **GET** `/api/v1/wallets/{id}/statement` - Request account statements.
    - **POST** `/api/v1/wallets/{id}/generate-qr` - Generate a QR code for the wallet.
    - **PUT** `/api/v1/wallets/{id}/set-pin` - Create or update the wallet PIN.
    - **PUT** `/api/v1/wallets/{id}/toggle-status` - Enable or disable the wallet.
    - **DELETE** `/api/v1/wallets/{id}` - Permanently delete the wallet.

---

## Built With

- **Spring Boot** - Backend framework.
- **MySQL** - Database for data persistence.
- **HikariCP** - Connection pool for optimized database interactions.
- **Hibernate** - ORM for entity management.

---

## Contributing

Contributions are welcome! Please submit a pull request or create an issue for suggestions or enhancements.

---

## License

Distributed under the MIT License. See `LICENSE` for more information.