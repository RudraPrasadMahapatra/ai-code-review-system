# High-Level Design (HLD) — AI Code Reviewer

## 1. Overview

The **AI Code Reviewer** is a web-based application that leverages OpenAI's GPT models to perform automated code reviews. Users submit code snippets via a web editor or file upload, and the system returns quality scores, complexity analysis, optimization suggestions, and detailed findings.

---

## 2. Architecture Style

The system follows a **Monolithic MVC Architecture** built on Spring Boot with server-side rendering via Thymeleaf. It exposes both a web UI and a REST API.

```
┌──────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                │
│                                                                      │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────────┐    │
│   │  Web Browser  │     │  REST Client │     │  Swagger/OpenAPI │    │
│   │  (Thymeleaf)  │     │  (JSON API)  │     │  (/swagger-ui)   │    │
│   └──────┬───────┘     └──────┬───────┘     └────────┬─────────┘    │
└──────────┼─────────────────────┼──────────────────────┼──────────────┘
           │                     │                      │
           ▼                     ▼                      ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER (Spring Boot)                  │
│                                                                      │
│   ┌────────────────────────────────────────────────────────────┐     │
│   │                     Controllers                             │     │
│   │  CodeReviewWebController   CodeReviewApiController          │     │
│   │  HomeController            CodeAnalysisController           │     │
│   └─────────────────────────┬──────────────────────────────────┘     │
│                             │                                        │
│   ┌─────────────────────────▼──────────────────────────────────┐     │
│   │                      Services                               │     │
│   │  AiCodeReviewService          CodeReviewService             │     │
│   └─────────────────────────┬──────────────────────────────────┘     │
│                             │                                        │
│   ┌─────────────────────────▼──────────────────────────────────┐     │
│   │                   AI Engine Layer                            │     │
│   │  «interface» CodeReviewEngine                               │     │
│   │       ├── OpenAiCodeReviewEngine  (GPT-4o-mini)             │     │
│   │       └── LocalReviewEngine       (Stub/Offline)            │     │
│   └────────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
           │                                        │
           ▼                                        ▼
┌─────────────────────┐              ┌─────────────────────────┐
│    DATA LAYER        │              │   EXTERNAL SERVICES      │
│                      │              │                          │
│  H2 (dev/in-memory)  │              │  OpenAI API (GPT-4o)     │
│  MySQL (production)  │              │  via Spring AI ChatClient│
│  Spring Data JPA     │              │                          │
└─────────────────────┘              └─────────────────────────┘
```

---

## 3. Key Components

| Component | Responsibility |
|---|---|
| **Web UI** | Thymeleaf-rendered code editor with CodeMirror, review results display |
| **REST API** | JSON endpoints for programmatic code review submissions |
| **Service Layer** | Orchestrates review flow: persist → call AI → parse → store results |
| **AI Engine** | Strategy pattern — swappable between OpenAI and local stub engine |
| **Data Layer** | JPA entities persisted to H2 (dev) or MySQL (prod) |
| **Exception Handler** | Global error handling with structured JSON responses |

---

## 4. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.5.x |
| AI Integration | Spring AI (OpenAI starter) |
| Template Engine | Thymeleaf |
| Code Editor | CodeMirror 5 (10 language modes) |
| Database | H2 (dev), MySQL (prod) |
| ORM | Hibernate 6 / Spring Data JPA |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build Tool | Maven |

---

## 5. Data Flow

```
User submits code
       │
       ▼
┌─────────────────┐
│  Controller      │──── Validates input (language, code)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Service         │──── Creates CodeReviewRecord (PENDING)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  CodeReviewEngine│──── Sends prompt to OpenAI API
└────────┬────────┘     Returns JSON: score, complexity,
         │              optimized code, findings
         ▼
┌─────────────────┐
│  Service         │──── Parses response, maps findings,
└────────┬────────┘     updates record (COMPLETED/FAILED)
         │
         ▼
┌─────────────────┐
│  Controller      │──── Returns response to UI/API
└─────────────────┘
```

---

## 6. Deployment Model

```
┌──────────────────────────────────────────┐
│              Docker Compose               │
│                                           │
│  ┌───────────────┐  ┌─────────────────┐  │
│  │  Spring Boot   │  │     MySQL 8      │  │
│  │  App (8081)    │──│  (3306)          │  │
│  └───────────────┘  └─────────────────┘  │
└──────────────────────────────────────────┘
```

- **Development**: Embedded H2 in-memory database, no external dependencies
- **Production**: MySQL via Docker Compose, environment-based API key injection

---

## 7. Non-Functional Requirements

| Aspect | Approach |
|---|---|
| **Scalability** | Stateless app — horizontal scaling behind a load balancer |
| **Availability** | Graceful degradation with `LocalReviewEngine` fallback |
| **Security** | API key via env vars, no secrets in code, input validation |
| **Performance** | Async AI calls possible, response caching for identical inputs |
| **Observability** | SLF4J logging, Spring Actuator (extendable) |
