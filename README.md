# URL Shortener Service

A production-ready URL shortening service built with Quarkus, featuring:
- 🚀 High-performance reactive architecture
- 🔐 OAuth2/OIDC authentication with Keycloak
- 💾 PostgreSQL database with Hibernate ORM
- ⚡ Redis/Valkey caching
- 📨 Apache Pulsar message streaming
- 🎯 RESTful API with OpenAPI/Swagger
- ☸️ Kubernetes-ready deployment

## 🏗️ Architecture

- **Framework:** Quarkus 3.x
- **Database:** PostgreSQL 16
- **Cache:** Valkey 7.2 (Redis-compatible)
- **Message Broker:** Apache Pulsar 3.1
- **Auth:** Keycloak 23.0
- **Deployment:** Kubernetes (Docker Desktop)

## 📚 Documentation

- [Kubernetes Deployment Guide](docs/KUBERNETES_DEPLOYMENT.md)
- [API Documentation](http://localhost:8080/q/swagger-ui) (when running)

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Docker Desktop with Kubernetes enabled
- kubectl CLI
- Maven 3.8+ (or use `./mvnw`)

### 1. Deploy Infrastructure

**Windows:**
```powershell
.\deploy-k8s.ps1
```

**Linux/Mac:**
```bash
./deploy-k8s.sh
```

### 2. Wait for Pods

```bash
kubectl get pods -n url-shortener -w
```

Wait until all pods are `Running`.

### 3. Run Application

```bash
./mvnw quarkus:dev -Dquarkus.profile=dev
```

### 4. Access Services

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/q/swagger-ui
- **Dev UI:** http://localhost:8080/q/dev
- **Keycloak:** http://localhost:30180

## 🌐 NodePort Services

| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL | 30432 | Database |
| Valkey | 30379 | Cache |
| Pulsar | 30650 | Message Broker |
| Pulsar Admin | 30081 | Admin UI |
| Keycloak | 30180 | Auth Server |
| Vault | 30200 | Secrets Management |
| APISIX Gateway | 30900 | API Gateway |
| APISIX Admin | 30901 | Admin API |
| APISIX Dashboard | 30910 | Web UI |

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/url-svc-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
