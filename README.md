# passStorage

A self-hosted web password manager for teams. Users sign in with their **Active Directory / LDAP** account, organise credentials into folders and share individual passwords with colleagues.

![Version](https://img.shields.io/badge/version-1.1.3-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-6DB33F)
![React](https://img.shields.io/badge/React-18-61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-required-336791)

## Features

- 🔐 **Active Directory login**: no separate accounts to manage; users authenticate against your domain controller.
- 📁 **Folders**: group passwords in nested folders.
- 🤝 **Sharing**: share a password with one or more users and see who currently has access.
- 🔑 **Per-user RSA keys**: a 2048-bit key pair is generated for each user, and passwords are stored encrypted in the database.
- 📋 **Copy to clipboard** from the web UI.
- ⚡ **Reactive stack**: Spring WebFlux + R2DBC, non-blocking end to end.
- 🗄️ **Automatic schema migrations** with Flyway.

## Tech stack

| Layer     | Technology                                                                 |
|-----------|----------------------------------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.1 (WebFlux, Security, Thymeleaf), Project Reactor   |
| Auth      | Spring Security LDAP (`ActiveDirectoryLdapAuthenticationProvider`)         |
| Database  | PostgreSQL via Spring Data R2DBC, Flyway migrations                        |
| Mapping   | MapStruct, Lombok                                                          |
| Frontend  | React 18, Material UI 5, bundled with Webpack                              |
| Packaging | Maven, Docker                                                              |

## Project structure

```
passStorage
├── src/main/java/ru/arbis29/passstorage
│   ├── config/        # Security (AD/LDAP), database/Flyway configuration
│   ├── controllers/   # REST controllers (passwords, folders, users) + root page
│   ├── domain/        # R2DBC entities
│   ├── mappers/       # MapStruct mappers
│   ├── model/         # DTOs
│   ├── repo/          # Reactive repositories
│   └── services/      # Business logic, key management and encryption
├── src/main/resources
│   ├── application.yml
│   ├── db/migration/postgres/   # Flyway SQL migrations
│   └── templates/rootApp.html   # Host page for the React app
├── front/pass-store/  # React single-page application
└── Dockerfile
```

## Getting started

### Prerequisites

- JDK 17+
- Node.js and npm (to build the frontend)
- A PostgreSQL database (for example `pass_storage`)
- An Active Directory / LDAP server reachable from the application

### Configuration

Settings live in `src/main/resources/application.yml`:

```yaml
ldap:
  url: ldap://server.DOMAIN.local:389     # domain controller
  domain: DOMAIN.local                    # AD domain used for login
  base: OU=UserFolder,DC=DOMAIN,DC=local  # LDAP search base
  adminRoles:                             # AD groups that get the APP_ADMIN role
spring:
  r2dbc:
    url: r2dbc:postgresql://pg-server-addr:5432/pass_storage
    username: postgres
    password:
  flyway:
    url: jdbc:postgresql://pg-server-addr:5432/pass_storage
server:
  port: 8080
```

Replace the placeholder values with your own. Any property can also be overridden with an environment variable (Spring relaxed binding), which is handy for Docker. For example:

```bash
export LDAP_URL=ldap://dc.example.local:389
export LDAP_DOMAIN=example.local
export SPRING_R2DBC_URL=r2dbc:postgresql://db:5432/pass_storage
export SPRING_R2DBC_USERNAME=postgres
export SPRING_R2DBC_PASSWORD=secret
export SPRING_FLYWAY_URL=jdbc:postgresql://db:5432/pass_storage
```

The database schema is created and upgraded automatically on startup.

### Build and run

1. **Build the frontend.** The bundle is written to `src/main/resources/static/built/`, which is git-ignored.

   ```bash
   cd front/pass-store
   npm install
   npm run build
   cd ../..
   ```

2. **Run the backend.**

   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```

3. Open <http://localhost:8080> and sign in with your domain credentials.

For frontend development, `npm run watch` in `front/pass-store` rebuilds the bundle on every change.

### Docker

The `Dockerfile` packages the jar produced by Maven, so build the frontend and the jar first:

```bash
cd front/pass-store && npm install && npm run build && cd ../..
./mvnw clean package
docker build --build-arg PROJECTVER=1.1.3 -t passstorage:1.1.3 .
docker run -p 8080:8080 \
  -e LDAP_URL=ldap://dc.example.local:389 \
  -e LDAP_DOMAIN=example.local \
  -e SPRING_R2DBC_URL=r2dbc:postgresql://db:5432/pass_storage \
  -e SPRING_R2DBC_USERNAME=postgres \
  -e SPRING_R2DBC_PASSWORD=secret \
  -e SPRING_FLYWAY_URL=jdbc:postgresql://db:5432/pass_storage \
  passstorage:1.1.3
```

`PROJECTVER` must match the `<version>` in `pom.xml`, because the entrypoint runs `passStorage-${PROJECTVER}.jar`.

### Tests

```bash
./mvnw test
```

## API

All endpoints require an authenticated user (HTTP Basic or form login). Paths are relative to the application root.

### Passwords: `/api/v1/pass`

| Method   | Path                          | Description                                   |
|----------|-------------------------------|-----------------------------------------------|
| `GET`    | `/list`                       | Passwords owned by or shared with the user    |
| `GET`    | `/empty`                      | Empty password template                       |
| `POST`   | `/`                           | Create or update a password                   |
| `DELETE` | `/`                           | Delete a password (owner only)                |
| `POST`   | `/share`                      | Set the list of users a password is shared with |
| `GET`    | `/shared_users/{pass_id}`     | Users a password is shared with               |

### Folders: `/api/v1/folder`

| Method   | Path     | Description                          |
|----------|----------|--------------------------------------|
| `GET`    | `/list`  | Own and shared folders               |
| `GET`    | `/empty` | Empty folder template                |
| `POST`   | `/`      | Create or update a folder            |
| `DELETE` | `/`      | Delete a folder                      |

### Users: `/api/v1/user`

| Method | Path       | Description                  |
|--------|------------|------------------------------|
| `GET`  | `/list`    | All known users              |
| `GET`  | `/current` | The signed-in user           |
| `GET`  | `/empty`   | Empty user template          |

## How it works

- A user's identity is derived from their AD distinguished name, in the form `domain.parts/login`.
- On first use, an RSA key pair is created for the user and stored in the `users_keys` table.
- Saving a password encrypts it with the owner's key and keeps only the ciphertext. When a password is shared, it is re-encrypted for each recipient and stored in `shared_passwords`.
- Every signed-in domain user gets the `APP_USER` role. Members of the groups listed in `ldap.adminRoles` additionally get `APP_ADMIN`.

## Security notes

This project is under active development. Before using it with real secrets, be aware of the following:

- User private keys are stored **unencrypted** in the `users_keys` table. Anyone with read access to the database can decrypt the stored passwords, so restrict database access and encrypt backups.
- Passwords are encrypted with raw RSA (`Cipher.getInstance("RSA")`), which limits the length of a password and uses no padding scheme you specify explicitly.
- CSRF protection is disabled in the security configuration.
- Key generation currently logs the generated keys at `WARN` level. Disable or remove this before deploying to production.
- Run the application behind HTTPS, since HTTP Basic is enabled and LDAP is configured with plain `ldap://` by default.

## Contributing

Issues and pull requests are welcome. Please run `./mvnw test` before submitting a change.

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
