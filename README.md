# Quantum Save — Backend API 🛡️📊

✨ Production-grade backend API for **Quantum Save**, a full-stack personal finance management platform designed to handle real-world financial workflows with strong security, clean architecture, and scalable foundations.

🔗 **Live Demo:** https://quantum-save-webapp.vercel.app/                                        
🔗 **Frontend repository:** https://github.com/azedta/quantum-save-webapp  

---

## 📌 Project Overview

Quantum Save’s backend is responsible for securely managing user accounts, financial data, and all business logic behind income tracking, expense tracking, categorization, reporting, and automation.

The system is built with a **multi-user, security-first mindset**, ensuring that every request is authenticated, authorized, and scoped correctly — while remaining ergonomic for frontend consumption.

---

## 🧰 Tech Stack

- **Language:** Java 21  
- **Framework:** Spring Boot 3.x  
- **Security:** Spring Security + JWT (stateless authentication), BCrypt  
- **Database:** PostgreSQL (Neon)  
- **ORM:** Spring Data JPA (Hibernate)  
- **Build Tool:** Maven  
- **Deployment:** Render  

---

## 🧠 Architectural & Domain Design Highlights

- **Stateless architecture:** All requests are authenticated independently using JWTs; no server-side sessions.
- **DTO-first API:** Controllers expose DTOs only, keeping persistence entities internal.
- **Category type enforcement:** Categories are explicitly typed as `income` or `expense`, preventing invalid financial associations.
- **Derived dashboard data:** Financial totals and summaries are computed on demand rather than stored, eliminating data drift.
- **Service-layer authority:** Core validation and rules live in services, not controllers.

---

## 🔐 Security Model

- JWT-based authentication enforced via a custom Spring Security filter.
- Clear separation of authentication (`401 Unauthorized`) vs authorization (`403 Forbidden`) failures.
- Passwords are hashed using BCrypt and never stored or logged in plain text.
- CORS configuration explicitly allows trusted frontend origins (local + Vercel previews).
- No sensitive user data is ever derived from client input — identity comes exclusively from JWT context.

---

## 🧱 Data Ownership & Multi-Tenant Isolation

Quantum Save is designed as a **true multi-tenant system**:

- User identity is always derived from the authenticated JWT.
- No endpoint accepts `userId` or `profileId` from the client.
- All reads and mutations are scoped server-side to the authenticated profile.
- Destructive actions verify ownership explicitly before execution.

This guarantees strict data isolation and prevents horizontal privilege escalation.

---

## 🧩 API Endpoints Overview

**Base Path:** `/api/v1.0`
Below are the main endpoints grouped by authentication.

### Public (No Authentication)

| Method | Endpoint | Description |
|------|--------|------------|
| GET | `/status` | Lightweight status endpoint |
| GET | `/health` | Health check |
| POST | `/register` | Create account (sends activation email) |
| POST | `/login` | Authenticate and receive JWT |
| GET | `/activate` | Activate account via email token |
| POST | `/resend-verification` | Resend activation email |

### Authenticated (JWT Required)

| Method | Endpoint | Description |
|------|--------|------------|
| GET | `/profile` | Get current user profile |
| GET | `/dashboard` | Derived totals and recent activity |
| GET | `/categories` | List user categories |
| POST | `/categories` | Create category |
| GET | `/categories/{type}` | Categories by type |
| PUT | `/categories/{id}` | Update category |
| GET | `/incomes` | Current-month incomes |
| POST | `/incomes` | Create income |
| DELETE | `/incomes/{id}` | Delete income |
| GET | `/expenses` | Current-month expenses |
| POST | `/expenses` | Create expense |
| DELETE | `/expenses/{id}` | Delete expense |
| POST | `/filter` | Filter transactions |
| GET | `/excel/download/income` | Download income Excel |
| GET | `/excel/download/expense` | Download expense Excel |
| GET | `/email/income-excel` | Email income report |
| GET | `/email/expense-excel` | Email expense report |

**Notes**
- All authenticated endpoints derive the user from the JWT and enforce **ownership** server-side.
- Excel downloads return a file stream (`Content-Disposition`), while email endpoints trigger server-side delivery.
- The filter endpoint supports both `income` and `expense` via the request body.
---

## 📦 DTO & Response Design

- **AuthDTO:** Login request payload.
- **AuthResponseDTO:** Authentication response containing JWT and user info.
- **ProfileDTO:** User profile data.
- **CategoryDTO:** Category metadata (`name`, `type`, `icon`, timestamps).
- **IncomeDTO / ExpenseDTO:** Financial transactions.
- **RecentTransactionDTO:** Normalized dashboard transaction model.
- **ApiResponse<T>:** Used for message-driven responses (e.g. deletions, confirmations).

The API does **not** force a single wrapper everywhere — response shapes are chosen to optimize frontend clarity and UX.

---

## 📊 Reporting & Automation

- **Excel exports:** Server-generated `.xlsx` reports using Apache POI.
- **Email delivery:** Reports and notifications sent via Brevo API (HTML + attachments).
- **Scheduled jobs:** Cron-based reminders and daily summaries handled server-side.

These features move the system beyond basic CRUD into real operational workflows.

---

## ⚠️ Error Handling Philosophy

- Meaningful HTTP status codes (`400`, `401`, `403`, `404`, `409`, `500`).
- Human-readable error messages intended for UI display.
- No silent failures or blind `204` responses.
- Consistent handling of edge cases (empty states, expired tokens, invalid ownership).

---

## 🗂️ Project Structure

```text
src/main/java/com/quantumsave/quantum_save/
├── config/
├── controller/
├── dto/
├── entity/
├── repository/
├── security/
├── service/
└── util/
```
---

## 🔑 Environment Variables

### Database
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### JWT
- `jwt.secret`
- `jwt.expiration`

### Email (Brevo)
- `brevo.api.key`
- `brevo.api.url`
- `brevo.sender.email`
- `brevo.sender.name`

### Frontend
- `quantum.save.frontend.url`

---

## ▶️ Run Locally / Deployment

### Run locally
```bash
mvn clean install
mvn spring-boot:run
```

API available at:
```
http://localhost:8080/api/v1.0
```

### Deployment
The backend is deployed on **Render** using environment-based configuration and a production PostgreSQL database.

---

## 📄 License

This project is proprietary and protected under an All Rights Reserved license.

The source code is provided for viewing and evaluation purposes only as part of a personal portfolio.
Any use, reproduction, modification, or distribution without explicit permission from the author is prohibited.
