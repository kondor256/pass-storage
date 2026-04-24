# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

passStorage is a Spring Boot 3.1.5 password vault application with a React frontend. It stores encrypted passwords in folders, supports sharing passwords between users, and authenticates via Active Directory LDAP.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.1.5, WebFlux (reactive), Spring Security (LDAP/AD auth), R2DBC + PostgreSQL, Flyway migrations, Lombok, MapStruct
- **Frontend**: React 18, MUI 5, Webpack (built to `src/main/resources/static/built/main.js`)
- **Infra**: Docker (openjdk:17-jdk-slim), Maven wrapper included

## Key Architecture

### Package Structure (`src/main/java/ru/arbis29/passstorage`)

- **`config/`** - Spring config: `SecurityConfig` (LDAP/AD auth, WebFlux security), `DatabaseConfig` (R2DBC), `CommonConfig`, `ldap/` (LdapGrantedAuthoritiesMapper)
- **`controllers/`** - REST controllers: `PasswordController` (`/api/v1/pass`), `FolderController` (`/api/v1/folder`), `UserController` (`/api/v1/user`), `MainController` (serves Thymeleaf template)
- **`domain/`** - R2DBC entities (implement `Persistable<String>`): `AppUser`, `StoredPassword`, `PasswordFolder`, `SharedPassword`, `UsersKeys`
- **`model/`** - DTOs: `PasswordDTO`, `FolderDTO`, `UserDTO`, `SharePassRequestDTO`
- **`mappers/`** - MapStruct mappers: `PasswordMapper`, `FolderMapper`, `UserMapper` (compiled to `*Impl` classes)
- **`repo/`** - R2DBC repositories: `PasswordRepo`, `FolderRepo`, `AppUserRepo`, `SharedPasswordRepo`, `KeysRepo`
- **`services/`** - Interface + Psql implementation pattern: e.g. `PasswordService` / `PasswordServicePsql`, `FolderService` / `FolderServicePsql`, `UserService` / `UserServicePsql`, `KeysService` / `KeysServicePsql`

### Database Schema (Flyway migrations `db/migration/postgres/V1_*`)

- `app_users` - Users (id, name, login)
- `password_folders` - Hierarchical folders with `folder_id` self-ref and `owner_user_id`
- `stored_passwords` - Password entries with `name`, `description`, `url`, `login`, `password`, `encrypted_password`, `owner_user_id`, `folder_id`
- `shared_passwords` - Many-to-many link with `user_id`, `password_id`, `encrypted_password`
- `users_keys` - Per-user key pair (`priv_key`, `pub_key`)

### Frontend Build

Frontend lives in `front/pass-store/` (React + MUI). Build output goes to `src/main/resources/static/built/main.js`.

```bash
cd front/pass-store
npm run build      # produces static/built/main.js
npm run watch      # webpack --watch for dev
npm start          # react-scripts dev server
npm test           # run tests
```

## Common Commands

```bash
# Build the project
./mvnw clean package

# Run tests
./mvnw test

# Run a single test
./mvnw test -Dtest=FolderControllerTest

# Run the app (dev mode)
./mvnw spring-boot:run

# Docker build
docker build --build-arg PROJECTVER=1.1.3 -t pass-storage .
```

## Testing

Tests use Spring Boot WebFlux test infrastructure:
- Controller tests: `@WebFluxTest` with `MockBean` repos, `WebTestClient` for assertions
- Service tests: `@SpringBootTest` with real repos or mocked dependencies
- Tests are in `src/test/java/ru/arbis29/passstorage/` mirroring the main package structure

## Configuration

`src/main/resources/application.yml` contains LDAP and PostgreSQL connection settings. The defaults reference placeholder addresses (`ldap://server.DOMAIN.local:389`, `pg-server-addr:5432`). These need to be overridden for actual deployment. CSRF is disabled for the reactive security config.

## Frontend-Backend Integration

The Thymeleaf template at `src/main/resources/templates/rootApp.html` mounts the React app. API calls go to `/api/v1/pass`, `/api/v1/folder`, `/api/v1/user` endpoints. The frontend is bundled via Webpack and served as static resources.
