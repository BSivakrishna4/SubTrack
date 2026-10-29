# SubTrack – DTI Requirement: Customer Journey Map

**Project Identity**: SubTrack – Subscription Lifecycle & Recurring Revenue Management System  
**Mapping Target**: End-to-End Proposed System Workflow

---

## Journey Stages Overview

| Stage | Stage Name | User Action | Touchpoint | User Pain Point | System Response | SubTrack Opportunity |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Discover Plan** | Browses available subscription plans | SubTrack Catalog (`/plans`) | Unclear pricing tiers | Displays transparent price, duration, features | Clear visual plan cards |
| **2** | **Register / Login** | Creates account & authenticates | Auth Portal (`/login`, `/register`) | Complex sign-up forms | Validates credentials, issues JWT token | Secure JWT authentication |
| **3** | **Select Plan** | Chooses desired plan duration | Plan Card (`/plans`) | Accidental selection | Prompts confirmation with pricing breakdown | Instant activation trigger |
| **4** | **Make Payment** | Submits mock payment details | Payment Gateway (`/api/payments`) | Payment gateway errors | Processes payment, generates `TXN-` reference | OpenFeign inter-service payment |
| **5** | **Activation** | Receives confirmation of active status | Dashboard (`/dashboard`) | Delayed activation | Sets status `ACTIVE`, records start & expiry dates | Dynamic status badge (`ACTIVE`) |
| **6** | **Use Service** | Utilizes subscription benefits | SubTrack Platform | Loss of expiry tracking | Displays start date, expiry date, active plan details | Live status overview |
| **7** | **Expiry Warning** | Views pre-expiry alert | Expiry Warning Banner | Surprise auto-renewal | Displays alert banner when expiring within 5 days | Innovative expiry alert banner |
| **8** | **Renew / Cancel** | Clicks Renew or Cancel button | Dashboard Action Buttons | Complicated cancellation | Extends expiry date (Renew) OR sets `CANCELLED` | 1-click renewal & cancellation |

---

## Detailed Stage Analysis

### Stage 1: Discover Subscription Plan
- **User Goal**: Compare duration (30, 90, 365 days) and pricing.
- **System Touchpoint**: React `/plans` catalog view connected to `/api/plans`.

### Stage 2: Register / Authentication
- **User Goal**: Securely sign in to manage plans.
- **System Touchpoint**: React `/login` connected to `/api/users/login` on `USER-SERVICE`.

### Stage 3 & 4: Select Plan & Payment Processing
- **User Goal**: Complete payment safely.
- **System Touchpoint**: `SUBSCRIPTION-SERVICE` communicates with `PAYMENT-SERVICE` via OpenFeign.

### Stage 5 & 6: Activation & Subscription Usage
- **User Goal**: Access plan features immediately.
- **System Touchpoint**: Status set to `ACTIVE`, start and expiry dates computed.

### Stage 7 & 8: Expiry Detection, Renewal & Cancellation
- **User Goal**: Maintain control over future billing.
- **System Touchpoint**: `@Scheduled` background task detects expired subscriptions; 1-click renewal/cancel API endpoints execute state transitions.
