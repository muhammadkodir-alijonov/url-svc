# Vault Integration Guide

## Overview

The URL Shortener application uses HashiCorp Vault to securely store and manage sensitive configuration data. **Secrets are automatically loaded from Vault at application startup** using the Quarkus Vault extension.

### Key Benefits:
- **Centralized Secret Management**: All secrets in one place
- **Automatic Configuration**: No manual secret injection needed
- **Environment Separation**: Different secrets for dev/prod
- **Dynamic Updates**: Secrets can be rotated without redeployment

## How It Works

### 1. Vault Storage Structure

Secrets are organized by environment:
```
secret/url-shortener/
├── dev/
│   ├── database/postgres      (DB credentials)
│   ├── keycloak/config         (OAuth/JWT config)
│   ├── redis/config            (Redis connection)
│   ├── pulsar/config           (Message broker)
│   └── application/config      (App settings)
└── prod/
    └── (same structure)
```

### 2. Automatic Configuration Loading

The application automatically reads secrets from Vault:

1. **Setup Script** (`scripts/setup-vault.sh`): Populates Vault with secrets
2. **Quarkus Extension**: Reads from configured Vault paths at startup
3. **Property Override**: Vault values override application.properties

**Important**: Secret keys in Vault must match Quarkus property names exactly!

Example:
```bash
# This key in Vault:
quarkus.datasource.password="mySecretPassword"

# Overrides this property:
quarkus.datasource.password=<value from properties file>
```

## Quick Start

### 1. Prerequisites

Make sure Vault is running in Kubernetes:
```bash
kubectl get pods -n url-shortener | grep vault
```

Vault should be accessible at: `http://localhost:30200`

### 2. Setup Vault

Run the setup script to initialize all secrets:

**Windows (PowerShell):**
```powershell
.\scripts\setup-vault.ps1
```

**Linux/Mac:**
```bash
chmod +x scripts/setup-vault.sh
./scripts/setup-vault.sh
```

### 3. Verify Secrets

Check that secrets were stored successfully:
```bash
# Set environment variables
export VAULT_ADDR='http://localhost:30200'
export VAULT_TOKEN='dev-root-token'

# List all secrets
vault kv list secret/url-shortener

# Get specific secret
vault kv get secret/url-shortener/database/postgres
```

## Using Vault in Code

### Inject VaultService

```java
@Inject
VaultService vaultService;
```

### Store a Secret

```java
// Store a single secret
vaultService.storeSecret("myapp/config", "api-key", "secret-value");

// Store multiple secrets
Map<String, String> secrets = Map.of(
    "username", "admin",
    "password", "secret123"
);
vaultService.storeSecrets("myapp/credentials", secrets);
```

### Retrieve a Secret

```java
// Get a single secret
String apiKey = vaultService.getSecret("myapp/config", "api-key");

// Get all secrets from a path
Map<String, String> allSecrets = vaultService.getSecrets("myapp/credentials");
String username = allSecrets.get("username");
```

### Check if Secret Exists

```java
boolean exists = vaultService.secretExists("myapp/config", "api-key");
```

### Delete a Secret

```java
vaultService.deleteSecret("myapp/config");
```

## Admin API Endpoints

The application provides REST endpoints for managing Vault secrets (admin role required):

### Get a Secret
```bash
GET /api/admin/vault/{path}/{key}

# Example
curl -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/api/admin/vault/database/postgres/username
```

### Get All Secrets from Path
```bash
GET /api/admin/vault/{path}

# Example
curl -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/api/admin/vault/database/postgres
```

### Store Secrets
```bash
POST /api/admin/vault/{path}
Content-Type: application/json

{
  "key1": "value1",
  "key2": "value2"
}

# Example
curl -X POST \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"new-key": "new-value"}' \
  http://localhost:8080/api/admin/vault/myapp/config
```

### Delete Secrets
```bash
DELETE /api/admin/vault/{path}

# Example
curl -X DELETE \
  -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/api/admin/vault/myapp/config
```

### Health Check
```bash
GET /api/admin/vault/health

# Example
curl http://localhost:8080/api/admin/vault/health
```

## Secret Paths

The following secret paths are configured:

| Path | Description |
|------|-------------|
| `database/postgres` | PostgreSQL credentials |
| `keycloak/config` | Keycloak/OIDC configuration |
| `redis/config` | Redis connection details |
| `pulsar/config` | Apache Pulsar settings |
| `apisix/config` | APISIX Gateway credentials |
| `vault/config` | Vault self-reference |
| `application/config` | Application settings |

## Vault UI

Access Vault UI at: `http://localhost:30200/ui`

**Login:**
- Method: Token
- Token: `dev-root-token`

## CLI Commands Reference

### Login to Vault
```bash
export VAULT_ADDR='http://localhost:30200'
export VAULT_TOKEN='dev-root-token'
vault login
```

### List Secrets
```bash
# List paths
vault kv list secret/url-shortener

# List nested paths
vault kv list secret/url-shortener/database
```

### Read Secrets
```bash
# Get all keys in a path
vault kv get secret/url-shortener/database/postgres

# Get specific field
vault kv get -field=username secret/url-shortener/database/postgres

# Get JSON output
vault kv get -format=json secret/url-shortener/database/postgres
```

### Write Secrets
```bash
# Write single secret
vault kv put secret/url-shortener/test key=value

# Write multiple secrets
vault kv put secret/url-shortener/test \
  key1=value1 \
  key2=value2
```

### Delete Secrets
```bash
# Delete latest version
vault kv delete secret/url-shortener/test

# Delete specific version
vault kv delete -versions=2 secret/url-shortener/test

# Permanently delete (unrecoverable)
vault kv destroy -versions=1 secret/url-shortener/test
```

### Secret Versions
```bash
# View metadata and versions
vault kv metadata get secret/url-shortener/database/postgres

# Get specific version
vault kv get -version=1 secret/url-shortener/database/postgres

# Undelete a version
vault kv undelete -versions=2 secret/url-shortener/database/postgres
```

## Configuration

Vault configuration in `application-dev.properties`:

```properties
# Vault Connection
quarkus.vault.url=http://localhost:30200
quarkus.vault.authentication.client-token=dev-root-token
quarkus.vault.secret-config-kv-path=secret/url-shortener
quarkus.vault.kv-secret-engine-version=2
quarkus.vault.tls.skip-verify=true
```

## Security Best Practices

### Development
- ✅ Use dev mode with root token
- ✅ Store all sensitive configs in Vault
- ✅ Use Vault UI for quick debugging

### Production
- ❌ Never use root token
- ✅ Use AppRole or Kubernetes auth
- ✅ Enable TLS/SSL
- ✅ Implement secret rotation
- ✅ Enable audit logging
- ✅ Use namespaces for multi-tenancy
- ✅ Implement least privilege access

## Troubleshooting

### Connection Issues
```bash
# Check Vault status
curl http://localhost:30200/v1/sys/health

# Check pod logs
kubectl logs -n url-shortener vault-0
```

### Permission Denied
```bash
# Verify token has correct permissions
vault token lookup

# Check secret engine is enabled
vault secrets list
```

### Secrets Not Found
```bash
# List all paths to verify
vault kv list secret/url-shortener

# Check if path exists
vault kv get secret/url-shortener/database/postgres
```

## Migration from Properties Files

To migrate existing properties to Vault:

1. **Identify sensitive properties** in `application.properties`
2. **Store in Vault** using the setup script or manually
3. **Update code** to use `VaultService`
4. **Remove sensitive data** from properties files
5. **Test thoroughly** before deploying

## Additional Resources

- [Vault Documentation](https://developer.hashicorp.com/vault/docs)
- [Quarkus Vault Guide](https://quarkus.io/guides/vault)
- [Vault Best Practices](https://developer.hashicorp.com/vault/tutorials/best-practices)

