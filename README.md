## SocialMedia

A full-featured backend service for a modern social media platform.
Written in Java and Spring Boot, built with Gradle, and containerized with Docker & Docker Compose.

---

## Tech Stack

- **Language:** Java 23
- **Build Tool:** Gradle 8.14.2
- **Frameworks:** Spring Boot, Spring Security, Spring Data JPA, Spring WebSocket
- **Databases:** PostgreSQL 17
- **Cache:** Redis 7.2.5
- **Search Engine:** Elasticsearch 7.17.28
- **Others:**
  - Hibernate
  - ModelMapper
  - JWT (Access + Refresh Token)
  - WebSocket (STOMP) Messaging
  - Swagger / OpenAPI 3
  - JUnit 5 + Mockito for testing
  - Rate Limiting with Bucket4J

---

## Description

The project implements a complete backend infrastructure for a social media platform supporting:
- User registration and authentication
- Friendship and invites
- Real-time messaging (WebSocket)
- Posting with optional image upload
- Like system and user profiles
- Caching and indexing for fast responses

---

## Features

- ✅ **JWT Auth (Access + Refresh tokens)**
- ✅ **HTTP-only Refresh Token Cookie for security**
- ✅ **Role-based authorization (Admin/Regular)**
- ✅ **User search with multilingual full-text (RU/EN)**
- ✅ **Image upload and static serving from Docker-mounted volume**
- ✅ **Real-time messaging via STOMP/WebSocket**
- ✅ **Rate limiting (1 message/sec) per user (Bucket4J)**
- ✅ **Over 200 unit tests with JUnit & Mockito (test coverage ~90%)**

---

## Swagger

Visit Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

---

## Quick Start

### 1. Prerequisites
- Docker
- Docker Compose

### 2. Run the project
```bash
docker compose up --build
```

All 4 services will be started:
- `spring` - the Java backend
- `database` - PostgreSQL
- `cache` - Redis
- `search` - Elasticsearch

---

## License

This project is developed for educational purposes. All rights reserved.

