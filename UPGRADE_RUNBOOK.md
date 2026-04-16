# Upgrade Runbook

This runbook is used while upgrading Angular, Spring Boot, Java, and Keycloak.

## Baseline Commands

### Backend

```bash
cd backend
mvn -q clean test
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run build
```

### Containerized stack

```bash
cp .env.example .env
docker compose up --build
```

## Smoke Test Checklist

Run these checks after every major migration step:

1. App login redirects to Keycloak.
2. `GET /shop/api/products` succeeds with a `user` token.
3. `GET /shop/api/customers` is forbidden for a `user` token.
4. Admin routes in the frontend are visible only to `admin` users.
5. User cart and order calls still work for non-admin users.
6. Playwright smoke suite passes against the compose stack.

## Rollback Checkpoints

- Checkpoint A: Baseline smoke test and docs established.
- Checkpoint B: Keycloak 26.6-compatible realm and URLs configured.
- Checkpoint C: Backend switched to OAuth2 resource server and JWT role mapping.
- Checkpoint D: Backend on Spring Boot 3 and Java 21.
- Checkpoint E: Frontend on latest Angular and modern Keycloak libs.
