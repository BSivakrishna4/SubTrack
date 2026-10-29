# SubTrack – Requirement Specification Document

**Project Identity**: SubTrack – Subscription Lifecycle & Recurring Revenue Management System  
**Document Version**: 1.0.0  
**Target Assessment**: Project Review-1 (Service-Oriented Architecture / Microservices)

---

## 1. Problem Statement & Background Analysis

### 1.1 Problem Statement
Modern digital businesses, SaaS providers, and content streaming platforms heavily rely on subscription-based recurring revenue models. Managing subscription lifecycles manually or through monolithic systems presents critical challenges:
- Difficulty in handling status transitions (`ACTIVE`, `EXPIRED`, `CANCELLED`).
- Inability to dynamically scale individual billing and authentication modules.
- Vulnerability to service downtime during payment processing peak loads.
- Lack of centralized routing and secure token-based authentication across services.

### 1.2 Proposed Microservices Solution
**SubTrack** solves these challenges using a Service-Oriented Architecture (SOA) consisting of loosely coupled microservices built with Spring Boot, Spring Cloud, MySQL/H2, and React + Vite:
1. **Eureka Discovery Server**: Dynamic lookup for all microservices without hardcoded IP configurations.
2. **Spring Cloud API Gateway**: Unified entry point handling CORS, request routing, and header propagation.
3. **User / Auth Service**: JWT authentication, BCrypt password hashing, and user profile management.
4. **Subscription Service**: Core business domain managing plans, activation, renewal, cancellation, and automated expiry detection.
5. **Payment Service**: Isolated mock payment processing engine storing transaction references.

---

## 2. Functional Requirements (FR)

| Requirement ID | Requirement Name | Description | Target Service |
| :--- | :--- | :--- | :--- |
| **FR-01** | User Registration | System must register users with valid name, email, role, and encrypted password. | `USER-SERVICE` |
| **FR-02** | User Authentication | System must authenticate user credentials against BCrypt hashes. | `USER-SERVICE` |
| **FR-03** | JWT Security | System must issue signed HMAC-SHA256 JWT tokens upon successful login. | `USER-SERVICE` / `API-GATEWAY` |
| **FR-04** | View Subscription Plans | System must display active subscription plans with price, description, and duration. | `SUBSCRIPTION-SERVICE` |
| **FR-05** | Activate Subscription | System must activate plan subscriptions only after mock payment validation. | `SUBSCRIPTION-SERVICE` |
| **FR-06** | Process Payment | System must process mock payments, generate unique `TXN-` references, and log results. | `PAYMENT-SERVICE` |
| **FR-07** | Track Subscription Status | System must track lifecycle states: `ACTIVE`, `EXPIRED`, `CANCELLED`. | `SUBSCRIPTION-SERVICE` |
| **FR-08** | Renew Subscription | System must process renewal payments and extend subscription expiry dates. | `SUBSCRIPTION-SERVICE` |
| **FR-09** | Cancel Subscription | System must allow explicit user cancellation, updating status to `CANCELLED`. | `SUBSCRIPTION-SERVICE` |
| **FR-10** | Detect Subscription Expiry | System must automatically detect and expire passed subscriptions using `@Scheduled` tasks. | `SUBSCRIPTION-SERVICE` |
| **FR-11** | Transaction History | System must provide transaction log records per user. | `PAYMENT-SERVICE` |
| **FR-12** | Admin Plan Management | Administrators must be able to create, update, and deactivate subscription plans. | `SUBSCRIPTION-SERVICE` |

---

## 3. Non-Functional Requirements (NFR)

- **Scalability**: Microservices are independently deployable and scalable horizontally.
- **Security**: Passwords must never be stored or transmitted in plain text. JWT secret keys must be configurable.
- **Maintainability**: Clear separation of concerns into Controller, Service, Repository, DTO, Entity, Exception layers.
- **Performance**: API Gateway latency should be under 50ms for proxying requests.
- **Fault Tolerance**: Microservice communication failures must return descriptive HTTP status codes and error payloads.

---

## 4. User Roles & System Constraints

### 4.1 User Roles
1. **Regular User**: Registers, logs in, selects subscription plans, makes mock payments, views active subscription, renews, cancels, and inspects payment history.
2. **Administrator**: Manages global plan catalog (Create, Edit, Deactivate).

### 4.2 Constraints & Assumptions
- Real payment gateway integrations (Stripe, Razorpay) are explicitly out of scope; a mock payment service is utilized.
- All service-to-service communication relies on Eureka service discovery over OpenFeign.
