# SubTrack – Architecture Documentation

**Project Identity**: SubTrack – Subscription Lifecycle & Recurring Revenue Management System  
**Architecture Pattern**: Microservices Architecture / Service-Oriented Architecture (SOA)

---

## 1. System Architecture Overview

SubTrack is built using a modern Spring Cloud Microservices pattern. All client requests pass through a single entry point—the **Spring Cloud API Gateway**—which routes traffic dynamically to backend microservices using **Netflix Eureka Service Discovery**.

```mermaid
graph TD
    Client["SubTrack UI (React + Vite)"] --> Gateway["API Gateway (Port 8080)"]
    Gateway --> Eureka["Eureka Discovery Server (Port 8761)"]
    
    Gateway -->|/api/users/**| UserSvc["User/Auth Service (Port 8081)"]
    Gateway -->|/api/plans/**, /api/subscriptions/**| SubSvc["Subscription Service (Port 8082)"]
    Gateway -->|/api/payments/**| PaySvc["Payment Service (Port 8083)"]
    
    UserSvc --> UserDB[("User MySQL / H2 DB")]
    SubSvc --> SubDB[("Subscription MySQL / H2 DB")]
    PaySvc --> PayDB[("Payment MySQL / H2 DB")]
    
    SubSvc -->|OpenFeign Inter-Service Call| PaySvc
```

---

## 2. Microservice Descriptions

### 2.1 Eureka Server (`eureka-server` — Port 8761)
Acts as the central service registry. Microservices register their hostname, IP, and port dynamically.

### 2.2 API Gateway (`api-gateway` — Port 8080)
Provides single entry point, CORS headers for React UI, and route matching (`lb://USER-SERVICE`, `lb://SUBSCRIPTION-SERVICE`, `lb://PAYMENT-SERVICE`).

### 2.3 User/Auth Service (`user-service` — Port 8081)
Manages user accounts, BCrypt password hashing, JWT token generation (`JwtUtil`), and user profile lookup.

### 2.4 Subscription Service (`subscription-service` — Port 8082)
Core business service managing subscription plans, user subscriptions, activation flow, renewal, cancellation, and `@Scheduled` background expiry detection.

### 2.5 Payment Service (`payment-service` — Port 8083)
Processes mock payment transactions, generates unique transaction references (`TXN-...`), and stores payment logs.

---

## 3. Core Architectural Sequence Diagrams

### 3.1 Subscription Activation Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor User as SubTrack UI User
    participant Gateway as API Gateway (8080)
    participant SubSvc as Subscription Service (8082)
    participant Feign as OpenFeign Client
    participant PaySvc as Payment Service (8083)
    participant DB as Subscription Database

    User->>Gateway: POST /api/subscriptions/activate (planId, userId)
    Gateway->>SubSvc: Route request to SUBSCRIPTION-SERVICE
    SubSvc->>SubSvc: Validate Plan & Duration Days
    SubSvc->>Feign: Invoke PaymentClient.processPayment()
    Feign->>PaySvc: POST /api/payments/process (amount, userId)
    PaySvc->>PaySvc: Generate TXN Reference & Save Payment
    PaySvc-->>Feign: Return PaymentResponse (status: SUCCESS/FAILED)
    Feign-->>SubSvc: Return Payment Status
    alt Payment SUCCESS
        SubSvc->>SubSvc: Compute Start & Expiry Date
        SubSvc->>DB: Save Subscription (Status: ACTIVE)
        SubSvc-->>User: 201 Created (Subscription ACTIVE)
    else Payment FAILED
        SubSvc-->>User: 402 Payment Required (Activation Aborted)
    end
```

### 3.2 Subscription Renewal Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor User as SubTrack UI User
    participant Gateway as API Gateway (8080)
    participant SubSvc as Subscription Service (8082)
    participant PaySvc as Payment Service (8083)

    User->>Gateway: PUT /api/subscriptions/{id}/renew
    Gateway->>SubSvc: Route request to SUBSCRIPTION-SERVICE
    SubSvc->>PaySvc: Process Mock Payment over OpenFeign
    PaySvc-->>SubSvc: Payment Response (SUCCESS)
    SubSvc->>SubSvc: Extend Expiry Date by Plan Duration Days
    SubSvc->>SubSvc: Set Status ACTIVE
    SubSvc-->>User: 200 OK (Renewed Subscription)
```

---

## 4. Subscription Lifecycle State Diagram

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Plan Selected
    DRAFT --> ACTIVE: Payment SUCCESS (Activate / Renew)
    DRAFT --> FAILED: Payment FAILED
    FAILED --> [*]
    ACTIVE --> CANCELLED: User Clicks Cancel
    ACTIVE --> EXPIRED: Expiry Date Passed (@Scheduled Task)
    EXPIRED --> ACTIVE: Renewal Payment SUCCESS
    CANCELLED --> [*]
```

---

## 5. Database ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        varchar role
        timestamp created_at
    }

    PLANS {
        bigint id PK
        varchar name
        varchar description
        numeric price
        integer duration_days
        boolean active
    }

    SUBSCRIPTIONS {
        bigint id PK
        bigint user_id FK
        bigint plan_id FK
        timestamp start_date
        timestamp expiry_date
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    PAYMENTS {
        bigint id PK
        bigint user_id FK
        bigint subscription_id FK
        numeric amount
        varchar payment_status
        varchar transaction_reference UK
        timestamp payment_date
    }

    USERS ||--o{ SUBSCRIPTIONS : "owns"
    PLANS ||--o{ SUBSCRIPTIONS : "defines"
    USERS ||--o{ PAYMENTS : "makes"
    SUBSCRIPTIONS ||--o{ PAYMENTS : "generates"
```
