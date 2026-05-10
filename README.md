# LiftKart 🛒

> **Your Cart, Elevated.** — A production-grade multi-vendor e-commerce platform built with Spring Boot microservices and React.

![LiftKart](https://img.shields.io/badge/LiftKart-E--Commerce-orange?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.14-brightgreen?style=for-the-badge&logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?style=for-the-badge&logo=react)
![Java](https://img.shields.io/badge/Java-22-red?style=for-the-badge&logo=openjdk)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql)

---

## 📌 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Test Accounts](#test-accounts)
- [Screenshots](#screenshots)

---

## Overview

LiftKart is a full-stack multi-vendor e-commerce platform where:

- 🧑‍💼 **Vendors** can register, list products, manage inventory and fulfil orders
- 🛍️ **Customers** can browse products, add to cart, checkout and track orders
- 👑 **Admins** can govern vendor approvals, manage categories and monitor platform analytics

Built as part of the **SE ZG503 Full Stack Application Development** assignment at **BITS Pilani WILP (M.Tech Software Engineering, 2025)**.

| Student | Saif Uddin Ahamed |
|---|---|
| BITS ID | 2025TM93168 |
| Programme | M.Tech Software Engineering — BITS Pilani WILP |
| Subject | SE ZG503 — Full Stack Application Development |

---

## Architecture

LiftKart follows a **microservices architecture** with 7 independent Spring Boot services communicating via REST, RabbitMQ, and Kafka.

```
                         ┌─────────────────────┐
                         │    React Frontend   │
                         │    (Port 3000)      │
                         └──────────┬──────────┘
                                    │ HTTP
                         ┌──────────▼──────────┐
                         │     API Gateway     │
                         │     (Port 8080)     │
                         │  Spring Cloud GW    │
                         └──┬──┬──┬──┬──┬──┬──┘
                            │  │  │  │  │  │
           ┌────────────────┘  │  │  │  │  └─────────────────┐
           │      ┌────────────┘  │  │  └──────────┐         │
           ▼      ▼               ▼  ▼             ▼         ▼
        ┌──────┐ ┌─────────┐ ┌──────┐ ┌───────┐ ┌──────┐ ┌──────────┐
        │ Auth │ │ Product │ │ Cart │ │ Order │ │ Notif│ │Analytics │
        │ 8081 │ │  8082   │ │ 8083 │ │ 8084  │ │ 8085 │ │   8086   │
        └──┬───┘ └────┬────┘ └──────┘ └───┬───┘ └──┬───┘ └────┬─────┘
           │          │                   │         │          │
           └──────────┴──── RabbitMQ ─────┘         │          │
                      │                             │          │
                      └──────────── Kafka ──────────┘──────────┘
```

### Services

| Service | Port | Responsibility |
|---|---|---|
| **API Gateway** | 8080 | Single entry point, CORS, routing |
| **Auth Service** | 8081 | Registration, login, JWT tokens, vendor approval |
| **Product Service** | 8082 | Catalogue, search, reviews, inventory |
| **Cart Service** | 8083 | Cart management with price snapshots |
| **Order Service** | 8084 | Order lifecycle, payments, addresses |
| **Notification Service** | 8085 | Event-driven in-app notifications |
| **Analytics Service** | 8086 | Sales dashboards, Kafka event log |

### Messaging

| Broker | Purpose | Topics/Queues |
|---|---|---|
| **RabbitMQ** | Task queues | Order notifications, vendor approval, low stock alerts |
| **Apache Kafka** | Event streaming | Order events, product view events |

---

## Tech Stack

### Backend
- **Java 22** + **Spring Boot 3.5.14**
- **Spring Cloud Gateway** (API Gateway — WebFlux)
- **Spring Security** + **JJWT 0.12.6** (JWT Auth)
- **Spring Data JPA** + **Hibernate 6**
- **Flyway** (versioned DB migrations)
- **SpringDoc OpenAPI 2.8** (Swagger UI)
- **RabbitMQ** + **Apache Kafka**
- **PostgreSQL 16** (one DB per service)
- **Maven 3.9** with parent POM

### Frontend
- **React 18** + **TypeScript**
- **TailwindCSS v3**
- **React Router v6**
- **Axios**
- **Recharts** (analytics charts)
- **React Hot Toast**

### DevOps
- **Docker** + **Docker Compose**
- **GitHub Actions** (CI/CD)
- **Multi-stage Dockerfiles**

---

## Features

### 🔐 Authentication
- JWT access tokens (24h) + refresh tokens (7 days)
- BCrypt password hashing (strength 12)
- Role-based access: `CUSTOMER`, `VENDOR`, `ADMIN`
- Vendor approval workflow

### 🛍️ Product Catalogue
- Hierarchical category tree
- Full-text search with category, price filters
- Customer reviews with star ratings
- Inventory management with audit log
- Low stock alerts via RabbitMQ

### 🛒 Cart & Checkout
- Persistent cart with price snapshots
- Save for later
- Multi-step checkout with address management
- Simulated payment (UPI, Card, COD)

### 📦 Orders
- Full lifecycle: `PENDING → CONFIRMED → SHIPPED → DELIVERED`
- Complete status history audit trail
- Address snapshot at order time
- Customer cancellation + admin override

### 🔔 Notifications
- Real-time in-app notifications via RabbitMQ
- Unread count badge
- Order updates, vendor approval alerts

### 📊 Analytics
- Vendor dashboard — revenue trends, order counts
- Admin platform dashboard — GMV, 30-day charts
- Powered by Kafka event streaming

---

## Project Structure

```
liftkart/
├── pom.xml                          # Parent Maven POM
├── docker-compose.yml               # Full stack container setup
├── start-all.sh                     # Start everything with one command
├── stop-all.sh                      # Stop all services
├── check-services.sh                # Health check all services
├── .github/
│   └── workflows/ci.yml             # GitHub Actions CI pipeline
├── docker/
│   ├── Dockerfile.spring            # Multi-stage Spring Boot image
│   ├── Dockerfile.react             # Nginx React image
│   └── init-databases.sql           # Create all DBs on startup
├── services/
│   ├── api-gateway/                 # Spring Cloud Gateway
│   ├── auth-service/                # JWT auth, user management
│   ├── product-service/             # Catalogue, search, inventory
│   ├── cart-service/                # Cart management
│   ├── order-service/               # Orders, payments, addresses
│   ├── notification-service/        # RabbitMQ consumer
│   └── analytics-service/           # Kafka consumer, dashboards
└── frontend/                        # React + TypeScript app
    └── src/
        ├── config/                  # Axios, API config
        ├── context/                 # Auth, Cart context providers
        ├── pages/
        │   ├── auth/                # Login, Register
        │   ├── customer/            # Home, Product, Cart, Checkout, Orders
        │   ├── vendor/              # Vendor dashboard
        │   └── admin/               # Admin dashboard
        └── types/                   # TypeScript type definitions
```

---

## Getting Started

### Prerequisites

- Java 22
- Maven 3.9+
- Node.js 20+
- Docker Desktop

### Quick Start

```bash
# Clone the repository
git clone https://github.com/onesaif/liftkart.git
cd liftkart

# Start infrastructure (PostgreSQL, RabbitMQ, Kafka)
docker-compose up postgres rabbitmq zookeeper kafka -d

# Start all backend services (Java 22 required)
export JAVA_HOME=$(/usr/libexec/java_home -v 22)  # macOS
bash start-all.sh

# Start frontend (separate terminal)
cd frontend
npm install
npm start
```

Wait ~90 seconds for all services to start, then open **http://localhost:3000**

### Service URLs

| Service | URL |
|---|---|
| LiftKart Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Auth Swagger | http://localhost:8081/swagger-ui.html |
| Product Swagger | http://localhost:8082/swagger-ui.html |
| Cart Swagger | http://localhost:8083/swagger-ui.html |
| Order Swagger | http://localhost:8084/swagger-ui.html |
| Notification Swagger | http://localhost:8085/swagger-ui.html |
| Analytics Swagger | http://localhost:8086/swagger-ui.html |
| RabbitMQ Dashboard | http://localhost:15672 (guest/guest) |

---

## API Documentation

All services expose Swagger UI at `/swagger-ui.html`. The API Gateway routes all requests through port 8080.

### Key Endpoints

```
POST   /api/auth/register              # Register customer
POST   /api/auth/register/vendor       # Register vendor
POST   /api/auth/login                 # Login
GET    /api/products                   # List all products
GET    /api/products/search            # Search with filters
POST   /api/cart/items                 # Add to cart
GET    /api/cart                       # Get cart
POST   /api/orders                     # Place order
GET    /api/orders                     # Get my orders
GET    /api/notifications              # Get notifications
GET    /api/analytics/vendor           # Vendor dashboard
GET    /api/analytics/platform         # Admin dashboard
```

---

## Test Accounts

| Email | Password | Role |
|---|---|---|
| admin@liftkart.com | Password123 | Admin |
| saif@liftkart.com | Password123 | Customer |
| saif-vendor@liftkart.com | Password123 | Vendor |

---

## Screenshots

| Home Page | Product Detail |
|---|---|
| Products grid with category filters | Full product info with Add to Cart |

| Cart | Order Detail |
|---|---|
| Items with quantity controls | Status timeline |

| Vendor Dashboard | Admin Dashboard |
|---|---|
| Revenue chart (Recharts) | Platform analytics bar chart |

---

## CI/CD

GitHub Actions pipeline runs on every push to `main`:
- Compiles all 7 Spring Boot services
- Builds React frontend
- Reports build status

See `.github/workflows/ci.yml` for configuration.

---

## License

Built for academic purposes — BITS Pilani WILP M.Tech SE ZG503 Assignment, May 2026.

---

<div align="center">
  Built with ❤️ using Spring Boot & React &nbsp;|&nbsp; Saif Uddin Ahamed &nbsp;|&nbsp; BITS Pilani WILP
</div>
