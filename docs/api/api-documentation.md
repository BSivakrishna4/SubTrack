# SubTrack – API Specification Document

**Project Identity**: SubTrack – Subscription Lifecycle & Recurring Revenue Management System  
**Base Gateway URL**: `http://localhost:8080`

---

## 1. User & Authentication APIs (`USER-SERVICE`)

### 1.1 Register User
- **HTTP Method**: `POST`
- **Gateway Endpoint**: `/api/users/register`
- **Purpose**: Creates a new user account with BCrypt password hashing.
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123",
  "role": "USER"
}
```
- **Response (`201 Created`)**:
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER",
  "createdAt": "2026-09-16T19:45:56.920"
}
```
- **Error Response (`409 Conflict`)**: Email already registered.

---

### 1.2 User Login
- **HTTP Method**: `POST`
- **Gateway Endpoint**: `/api/users/login`
- **Purpose**: Authenticates credentials and returns JWT token.
- **Request Body**:
```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```
- **Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6MSwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIn0...",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER"
}
```

---

## 2. Subscription Plan APIs (`SUBSCRIPTION-SERVICE`)

### 2.1 View Active Plans
- **HTTP Method**: `GET`
- **Gateway Endpoint**: `/api/plans`
- **Response (`200 OK`)**:
```json
[
  {
    "id": 1,
    "name": "Basic Monthly",
    "description": "Standard access for individual subscribers",
    "price": 199.00,
    "durationDays": 30,
    "active": true
  }
]
```

---

## 3. Subscription Lifecycle APIs (`SUBSCRIPTION-SERVICE`)

### 3.1 Activate Subscription
- **HTTP Method**: `POST`
- **Gateway Endpoint**: `/api/subscriptions/activate`
- **Request Body**:
```json
{
  "userId": 1,
  "planId": 1,
  "simulatePaymentFailure": false
}
```
- **Response (`201 Created`)**:
```json
{
  "id": 1,
  "userId": 1,
  "plan": {
    "id": 1,
    "name": "Basic Monthly",
    "price": 199.00,
    "durationDays": 30
  },
  "startDate": "2026-09-16T19:52:57.517",
  "expiryDate": "2026-10-16T19:52:57.517",
  "status": "ACTIVE",
  "lastPayment": {
    "paymentId": 1,
    "transactionReference": "TXN-1789568577964-32A145F2",
    "status": "SUCCESS",
    "amount": 199.00
  }
}
```
- **Error Response (`402 Payment Required`)**: Mock payment failure.

---

### 3.2 Renew Subscription
- **HTTP Method**: `PUT`
- **Gateway Endpoint**: `/api/subscriptions/{id}/renew`
- **Purpose**: Extends plan expiry date upon successful mock payment.
- **Response (`200 OK`)**: Updated subscription object with extended `expiryDate` and status `ACTIVE`.

---

### 3.3 Cancel Subscription
- **HTTP Method**: `PUT`
- **Gateway Endpoint**: `/api/subscriptions/{id}/cancel`
- **Purpose**: Transitions subscription status to `CANCELLED`.
- **Response (`200 OK`)**: Updated subscription object with status `CANCELLED`.

---

## 4. Mock Payment APIs (`PAYMENT-SERVICE`)

### 4.1 Process Mock Payment
- **HTTP Method**: `POST`
- **Gateway Endpoint**: `/api/payments/process`
- **Request Body**:
```json
{
  "userId": 1,
  "subscriptionId": 10,
  "amount": 499.00,
  "simulateFailure": false
}
```
- **Response (`201 Created`)**:
```json
{
  "paymentId": 1001,
  "transactionReference": "TXN-1789568577964-32A145F2",
  "status": "SUCCESS",
  "amount": 499.00,
  "userId": 1,
  "subscriptionId": 10
}
```
