# AI Code Review (Spring Boot)

Skeleton Spring Boot app for an AI-powered code review system (Java 17 + Maven).

## Run

```bash
./mvnw spring-boot:run
```

## Run (MySQL)

Start MySQL (Docker):

```bash
docker compose up -d
```

Run the app using the `mysql` profile:

```bash
export SPRING_PROFILES_ACTIVE=mysql
./mvnw spring-boot:run
```

MySQL config lives in `src/main/resources/application-mysql.yml` (defaults: database/user/password `aicodereview`).
Override with env vars: `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`.

## Run (OpenAI via Spring AI)

```bash
export OPENAI_API_KEY="..."
export SPRING_PROFILES_ACTIVE=openai
./mvnw spring-boot:run
```

## Run (MySQL + OpenAI)

```bash
export OPENAI_API_KEY="..."
export SPRING_PROFILES_ACTIVE=mysql,openai
./mvnw spring-boot:run
```

## API (example)

- `POST /api/reviews` – submit code for review
- `GET /api/reviews/{id}` – fetch a review + findings
- UI: `GET /review` – submit code and see feedback

The AI engine is currently a stub (`StubAiReviewEngine`) to demonstrate the layering.
