# Vault & APISIX Setup Guide

## 🔐 HashiCorp Vault

### Access
- **URL:** http://localhost:30200
- **Root Token:** `dev-root-token` (dev mode only!)

### Initial Setup

```bash
# Set environment variable
export VAULT_ADDR='http://localhost:30200'
export VAULT_TOKEN='dev-root-token'

# Check status
vault status

# Enable secrets engine
vault secrets enable -path=secret kv-v2

# Store database password
vault kv put secret/database password=admin123 username=admin

# Store Keycloak secret
vault kv put secret/keycloak client-secret="**my sec keyyyy**"

# Read secret
vault kv get secret/database
```

### Integration with Application

Add to `pom.xml`:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-vault</artifactId>
</dependency>
```

Add to `application.yml`:
```yaml
quarkus:
  vault:
    url: http://localhost:30200
    authentication:
      kubernetes:
        role: url-svc
    secret-config-kv-path: secret/database
```

---

## 🚪 Apache APISIX Gateway

### Access URLs
- **Gateway:** http://localhost:30900
- **Admin API:** http://localhost:30901
- **Dashboard:** http://localhost:30910

### Dashboard Login
- **Username:** `admin`
- **Password:** `admin`

### Create Route via Admin API

```bash
# Create upstream (your application)
curl "http://localhost:30901/apisix/admin/upstreams/1" \
  -H "X-API-KEY: admin-api-key" \
  -X PUT -d '{
    "type": "roundrobin",
    "nodes": {
      "host.docker.internal:8080": 1
    }
  }'

# Create route
curl "http://localhost:30901/apisix/admin/routes/1" \
  -H "X-API-KEY: admin-api-key" \
  -X PUT -d '{
    "uri": "/api/*",
    "upstream_id": 1,
    "plugins": {
      "limit-count": {
        "count": 100,
        "time_window": 60,
        "rejected_code": 429
      },
      "prometheus": {}
    }
  }'

# Test route
curl http://localhost:30900/api/health
```

### Enable Plugins

#### Rate Limiting
```bash
curl "http://localhost:30901/apisix/admin/routes/1" \
  -H "X-API-KEY: admin-api-key" \
  -X PATCH -d '{
    "plugins": {
      "limit-count": {
        "count": 100,
        "time_window": 60,
        "rejected_code": 429,
        "key": "remote_addr"
      }
    }
  }'
```

#### CORS
```bash
curl "http://localhost:30901/apisix/admin/routes/1" \
  -H "X-API-KEY: admin-api-key" \
  -X PATCH -d '{
    "plugins": {
      "cors": {
        "allow_origins": "*",
        "allow_methods": "GET,POST,PUT,DELETE,OPTIONS",
        "allow_headers": "*"
      }
    }
  }'
```

#### JWT Authentication
```bash
curl "http://localhost:30901/apisix/admin/routes/1" \
  -H "X-API-KEY: admin-api-key" \
  -X PATCH -d '{
    "plugins": {
      "jwt-auth": {
        "key": "user-key",
        "secret": "my-secret-key"
      }
    }
  }'
```

#### OpenID Connect (Keycloak)
```bash
curl "http://localhost:30901/apisix/admin/routes/1" \
  -H "X-API-KEY: admin-api-key" \
  -X PATCH -d '{
    "plugins": {
      "openid-connect": {
        "client_id": "shortener-backend",
        "client_secret": "**my sec keyyyy**",
        "discovery": "http://keycloak:8080/realms/url-shortener/.well-known/openid-configuration",
        "scope": "openid profile",
        "bearer_only": true,
        "realm": "url-shortener"
      }
    }
  }'
```

### Monitoring

Prometheus metrics available at:
```
http://localhost:30902/apisix/prometheus/metrics
```

---

## 🔄 Complete Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       v
┌──────────────────┐
│  APISIX Gateway  │  ← Rate limit, Auth, CORS
│  (localhost:30900)│
└──────┬───────────┘
       │
       v
┌──────────────────┐
│  Your App:8080   │
│  (url-svc)       │
└──────┬───────────┘
       │
       ├──> PostgreSQL (30432)
       ├──> Valkey (30379)
       ├──> Pulsar (30650)
       ├──> Keycloak (30180) ──> Vault (secrets)
       └──> Vault (30200)
```

---

## 📊 Production Best Practices

### Vault
- ✅ Use auto-unseal with cloud KMS
- ✅ Enable audit logging
- ✅ Use AppRole authentication
- ✅ Rotate secrets regularly
- ❌ Don't use dev mode in production!

### APISIX
- ✅ Enable HTTPS/TLS
- ✅ Use etcd cluster (3+ nodes)
- ✅ Configure rate limiting per route
- ✅ Enable access logs
- ✅ Use service discovery (Consul/Nacos)
- ✅ Set up monitoring (Prometheus + Grafana)

---

## 🐛 Troubleshooting

### Vault Not Starting
```bash
kubectl logs vault-0 -n url-shortener
kubectl describe pod vault-0 -n url-shortener
```

### APISIX Gateway Not Responding
```bash
# Check etcd
kubectl logs -l app=etcd -n url-shortener

# Check APISIX
kubectl logs -l app=apisix -n url-shortener

# Test etcd connection
kubectl exec -it deploy/apisix -n url-shortener -- curl http://etcd:2379/health
```

### Cannot Access Dashboard
```bash
kubectl get svc apisix-admin -n url-shortener
kubectl port-forward svc/apisix-admin 9000:9000 -n url-shortener
```

---

## 🚀 Quick Test

```bash
# 1. Deploy all services
./deploy-k8s.sh

# 2. Wait for pods
kubectl get pods -n url-shortener -w

# 3. Test Vault
curl http://localhost:30200/v1/sys/health

# 4. Test APISIX
curl http://localhost:30900/apisix/status

# 5. Access Dashboard
open http://localhost:30910
```

---

**Vault Token:** `dev-root-token`  
**APISIX Admin Key:** `admin-api-key`  
**Dashboard Credentials:** admin / admin
