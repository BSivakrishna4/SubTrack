# SubTrack – Subscription Lifecycle & Recurring Revenue Management System

> **Project Identity**: SubTrack  
> **Full Title**: SubTrack – Subscription Lifecycle & Recurring Revenue Management System  
> **Architecture Pattern**: Service-Oriented Architecture (SOA) / Microservices Architecture  
> **Tech Stack**: Java 21, Spring Boot 3.2.5, Spring Cloud (Eureka, Gateway, OpenFeign), JWT Security, MySQL / H2, React + Vite

---

## 📌 Project Overview

**SubTrack** is a subscription lifecycle management platform designed to handle recurring billing plans, user activations, mock payment processing, renewals, cancellations, automated expiry detection, and recurring revenue information.

The architecture is built on a loosely coupled microservices pattern with a single entry point through **Spring Cloud API Gateway** and service discovery via **Netflix Eureka Server**.

---

## 🏗️ Architecture & Microservices Matrix

```text
                               ┌─────────────────────┐
                               │     SubTrack UI     │
                               │    React + Vite     │
                               └──────────┬──────────┘
                                          │  HTTP Requests (Port 5173 -> 8080)
                                          ▼
                               ┌─────────────────────┐
                               │     API Gateway     │
                               │   Spring Gateway    │ (Port 8080)
                               └──────────┬──────────┘
                                          │
                 ┌────────────────────────┼────────────────────────┐
                 │                        │                        │
                 ▼                        ▼                        ▼
         ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
         │   User/Auth   │        │ Subscription  │        │    Payment    │
         │    Service    │        │    Service    │        │    Service    │
         │  (Port 8081)  │        │  (Port 8082)  │        │  (Port 8083)  │
         └───────┬───────┘        └───────┬───────┘        └───────┬───────┘
                 │                        │                        │
                 ▼                        ▼                        ▼
            User Database       Subscription Database       Payment Database

                               ┌─────────────────────┐
                               │    Eureka Server    │
                               │  Service Discovery  │ (Port 8761)
                               └─────────────────────┘

Subscription Service ───(OpenFeign Call)───► Payment Service
```

| Component / Microservice | Port | Service Name | Key Responsibilities |
| :--- | :---: | :--- | :--- |
| **Eureka Server** | `8761` | `eureka-server` | Service registration and dynamic lookup. |
| **API Gateway** | `8080` | `api-gateway` | Single entry point, routing (`lb://`), global CORS. |
| **User/Auth Service** | `8081` | `USER-SERVICE` | User registration, login, BCrypt hashing, JWT generation. |
| **Subscription Service** | `8082` | `SUBSCRIPTION-SERVICE` | Plan catalog, activation, renewal, cancel, `@Scheduled` expiry. |
| **Payment Service** | `8083` | `PAYMENT-SERVICE` | Mock payment processing, transaction reference generation (`TXN-`). |
| **Frontend UI** | `5173` | `subtrack-frontend` | React + Vite UI with Dashboard, Plans, and History. |

---

## ⚙️ Key Features

1. **JWT Authentication & Password Hashing**: Passwords encrypted using BCrypt; HMAC-SHA256 tokens secure protected APIs.
2. **Subscription Lifecycle Management**: Tracks states (`ACTIVE`, `EXPIRED`, `CANCELLED`) with computed start and expiry dates.
3. **Mock Payment Engine**: Generates unique `TXN-` references and logs transaction logs.
4. **OpenFeign Communication**: Subscription Service communicates with Payment Service dynamically over OpenFeign.
5. **Automated Expiry Scheduler**: `@Scheduled` task periodically scans and transitions expired active items to `EXPIRED`.
6. **Modern React Frontend**: Glassmorphic UI with dynamic status badges, pre-expiry warning alerts, and instant action controls.

---

## 🚀 How to Run the Project Locally

### Prerequisites
- **Java**: 21 LTS
- **Maven**: 3.9+
- **Node.js**: 18+ & npm

### Step 1: Start Eureka Server
```bash
cd eureka-server
mvn clean package -DskipTests
java -jar target/eureka-server-0.0.1-SNAPSHOT.jar
# Dashboard available at http://localhost:8761
```

### Step 2: Start User Service
```bash
cd user-service
mvn clean package -DskipTests
java -jar target/user-service-0.0.1-SNAPSHOT.jar
# Listens on port 8081
```

### Step 3: Start Payment Service
```bash
cd payment-service
mvn clean package -DskipTests
java -jar target/payment-service-0.0.1-SNAPSHOT.jar
# Listens on port 8083
```

### Step 4: Start Subscription Service
```bash
cd subscription-service
mvn clean package -DskipTests
java -jar target/subscription-service-0.0.1-SNAPSHOT.jar
# Listens on port 8082
```

### Step 5: Start API Gateway
```bash
cd api-gateway
mvn clean package -DskipTests
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
# Single Entry Point on port 8080
```

### Step 6: Start React Frontend
```bash
cd frontend
npm install
npm run dev
# Access UI at http://localhost:5173
```

---

## 📜 Team & University Placeholders

- **University**: [ PLACEHOLDER: e.g. KL University / SRM / Amrita ]
- **Department**: [ PLACEHOLDER: Department of Computer Science & Engineering ]
- **Team Name**: [ PLACEHOLDER: SubTrack Team ]
- **Team Lead**: [ PLACEHOLDER: Lead Student Name ]
- **Team Members**: [ PLACEHOLDER: Member 1, Member 2, Member 3 ]
