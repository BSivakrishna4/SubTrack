# SubTrack – Comprehensive System Test Plan

**Project Identity**: SubTrack – Subscription Lifecycle & Recurring Revenue Management System  
**Test Suite Coverage**: Unit, Integration, API, & End-to-End Lifecycle Verification

---

## 1. Automated & Manual Test Cases Matrix

| Test ID | Scenario Description | Target Endpoint / Action | Expected Result | Result |
| :---: | :--- | :--- | :--- | :---: |
| **TC-01** | Successful User Registration | `POST /api/users/register` | User created (201 Created), BCrypt password hashed. | **PASS** |
| **TC-02** | Duplicate Registration | `POST /api/users/register` (existing email) | Rejected with 409 Conflict & clean error message. | **PASS** |
| **TC-03** | Successful User Login | `POST /api/users/login` | Returns 200 OK with valid JWT token & user info. | **PASS** |
| **TC-04** | Invalid Password Login | `POST /api/users/login` (wrong pass) | Rejected with 401 Unauthorized. | **PASS** |
| **TC-05** | View Plan Catalog | `GET /api/plans` | Returns list of active plans (Basic, Pro, Enterprise). | **PASS** |
| **TC-06** | Successful Payment | `POST /api/payments/process` | Generates `TXN-` ref & status `SUCCESS`. | **PASS** |
| **TC-07** | Failed Payment Simulation | `POST /api/payments/process` (`simulateFailure: true`) | Generates `TXN-` ref & status `FAILED`. | **PASS** |
| **TC-08** | Subscription Activation (Payment Success) | `POST /api/subscriptions/activate` | Feign call succeeds -> Status set to `ACTIVE`. | **PASS** |
| **TC-09** | Activation Abort (Payment Failure) | `POST /api/subscriptions/activate` (`simulateFailure: true`) | Payment `FAILED` -> Returns 402 Payment Required. | **PASS** |
| **TC-10** | Fetch Active Subscription | `GET /api/subscriptions/user/1` | Returns subscription object with status & expiry. | **PASS** |
| **TC-11** | Renewal Flow | `PUT /api/subscriptions/1/renew` | Feign payment succeeds -> Expiry date extended. | **PASS** |
| **TC-12** | Cancellation Flow | `PUT /api/subscriptions/1/cancel` | Status set to `CANCELLED`. | **PASS** |
| **TC-13** | Automated Expiry Detection | `@Scheduled` Expiry Task | Task identifies expired items -> Updates status `EXPIRED`. | **PASS** |
| **TC-14** | API Gateway Routing | Single Port `8080` Proxied Requests | Gateway proxies requests to target microservices. | **PASS** |
| **TC-15** | Service Discovery | Eureka Dashboard (`http://localhost:8761`) | All 4 services appear registered with status `UP`. | **PASS** |
| **TC-16** | Frontend React Navigation | Vite UI (`http://localhost:5173`) | Full user journey works smoothly via Axios client. | **PASS** |

---

## 2. Postman Collection Testing Guide

To test using Postman:
1. Import `SubTrack_Postman_Collection.json` into Postman.
2. Set Environment Variable `gateway_url` = `http://localhost:8080`.
3. Execute `1. Register User` $\rightarrow$ `2. Login User` (Copy `token` to Bearer token header).
4. Execute `3. Get Plans` $\rightarrow$ `4. Activate Plan` $\rightarrow$ `5. Renew Subscription` $\rightarrow$ `6. Cancel Subscription`.
