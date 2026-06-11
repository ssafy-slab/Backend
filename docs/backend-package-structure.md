# Backend Package Structure

This backend uses feature-first packages. Each feature owns its API, business logic, persistence contracts, DTOs, and domain objects in one place.

## Java Packages

```text
com.ssafy.ssafy_slap
├─ global
│  ├─ config
│  ├─ exception
│  ├─ response
│  ├─ security
│  └─ util
├─ user
├─ place
├─ trip
├─ schedule
├─ chat
├─ ai
├─ vote
├─ checklist
├─ community
├─ review
├─ report
└─ admin
```

Each feature package follows the same inner layout:

```text
feature
├─ controller
├─ service
├─ repository
├─ mapper
├─ dto
└─ domain
```

## Layer Roles

- `controller`: REST or WebSocket entry points.
- `service`: business rules and transaction boundaries.
- `repository`: persistence-facing components used by services.
- `mapper`: MyBatis mapper interfaces.
- `dto`: request and response objects.
- `domain`: database-backed domain records, entities, and enums.

## Resource Layout

MyBatis XML mapper files should mirror the Java feature packages:

```text
src/main/resources/mapper
├─ user
├─ place
├─ trip
├─ schedule
├─ chat
├─ ai
├─ vote
├─ checklist
├─ community
├─ review
├─ report
└─ admin
```

## Collaboration Rule

When adding a feature, start inside its feature package. Put cross-cutting code in `global` only when at least two features need it.
