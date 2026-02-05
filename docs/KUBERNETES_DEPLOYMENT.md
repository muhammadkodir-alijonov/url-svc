# URL Shortener - Kubernetes Deployment Guide

## 📋 Prerequisites

- Docker Desktop with Kubernetes enabled
- kubectl CLI installed
- 8GB RAM minimum (recommended: 16GB)

## 🚀 Quick Start

### 1. Deploy Infrastructure

**Windows (PowerShell):**
```powershell
.\deploy-k8s.ps1
```

**Linux/Mac/WSL:**
```bash
chmod +x *.sh
./deploy-k8s.sh
```

### 2. Check Status

**Windows:**
```powershell
.\check-k8s-status.ps1
```

**Linux/Mac/WSL:**
```bash
./check-k8s-status.sh
```

### 3. Wait for Pods to be Ready

```bash
kubectl get pods -n url-shortener -w
```

Wait until all pods show `READY 1/1` and `STATUS Running`.

## 🔧 Troubleshooting

### ImagePullBackOff Error

If you see `ImagePullBackOff` for any pod:

```bash
# Check pod details
kubectl describe pod postgres-0 -n url-shortener

# Check events
kubectl get events -n url-shortener --sort-by='.lastTimestamp'

# Pull images manually
docker pull postgres:16-alpine
docker pull valkey/valkey:7.2-alpine
docker pull apachepulsar/pulsar:3.1.0
docker pull quay.io/keycloak/keycloak:23.0
```

### Pod Stuck in ContainerCreating

```bash
# Check pod events
kubectl describe pod POD_NAME -n url-shortener

# Check PVC status
kubectl get pvc -n url-shortener

# Check node resources
kubectl top nodes
```

### View Logs

```bash
# PostgreSQL
kubectl logs postgres-0 -n url-shortener

# Valkey
kubectl logs valkey-0 -n url-shortener

# Pulsar
kubectl logs pulsar-0 -n url-shortener

# Keycloak
kubectl logs -f deployment/keycloak -n url-shortener
```

## 🌐 Access URLs (NodePort)

Once all pods are running:

| Service | URL | Purpose |
|---------|-----|---------|
| PostgreSQL | `localhost:30432` | Database |
| Valkey (Redis) | `localhost:30379` | Cache |
| Pulsar | `localhost:30650` | Message Broker |
| Pulsar Admin | `localhost:30081` | Pulsar Dashboard |
| Keycloak | `http://localhost:30180` | Auth Server |

### Keycloak Setup

1. Open: http://localhost:30180
2. Login: `admin` / `admin`
3. Create realm: `url-shortener`
4. Create client: `shortener-backend`
5. Copy client secret to `application-dev.yml`

## 📦 Infrastructure Components

### PostgreSQL (StatefulSet)
- **Image:** `postgres:16-alpine`
- **Port:** 30432
- **Storage:** 5Gi PVC
- **Credentials:** admin/admin123

### Valkey/Redis (StatefulSet)
- **Image:** `valkey/valkey:7.2-alpine`
- **Port:** 30379
- **Storage:** 1Gi PVC

### Pulsar (StatefulSet)
- **Image:** `apachepulsar/pulsar:3.1.0`
- **Port:** 30650 (broker), 30081 (admin)
- **Storage:** 5Gi PVC
- **Mode:** Standalone

### Keycloak (Deployment)
- **Image:** `quay.io/keycloak/keycloak:23.0`
- **Port:** 30180
- **Admin:** admin/admin

### Vault (StatefulSet)
- **Image:** `hashicorp/vault:1.15`
- **Port:** 30200
- **Root Token:** dev-root-token (dev mode)
- **Storage:** 1Gi PVC

### APISIX Gateway (Deployment)
- **Image:** `apache/apisix:3.7.0-debian`
- **Ports:** 30900 (gateway), 30901 (admin), 30902 (metrics)
- **Dashboard:** `apache/apisix-dashboard:3.0.1-alpine` (port 30910)
- **Dependencies:** etcd 3.5

## 🏃 Running the Application

### Development Mode

```bash
# Windows
.\mvnw.cmd quarkus:dev -Dquarkus.profile=dev

# Linux/Mac
./mvnw quarkus:dev -Dquarkus.profile=dev
```

The application will connect to Kubernetes infrastructure via NodePorts.

### Configuration

Edit `src/main/resources/application-dev.yml`:

```yaml
quarkus:
  datasource:
    jdbc:
      url: jdbc:postgresql://localhost:30432/url_shortener
  redis:
    hosts: redis://localhost:30379
  pulsar:
    client:
      serviceUrl: pulsar://localhost:30650
  oidc:
    auth-server-url: http://localhost:30180/realms/url-shortener
```

## 🗑️ Clean Up

**Windows:**
```powershell
.\delete-k8s.ps1
```

**Linux/Mac:**
```bash
./delete-k8s.sh
```

Or manually:
```bash
kubectl delete namespace url-shortener
```

## 📊 Monitoring

### Watch All Resources
```bash
watch kubectl get all -n url-shortener
```

### Get Pod Status
```bash
kubectl get pods -n url-shortener -o wide
```

### Check Resource Usage
```bash
kubectl top pods -n url-shortener
kubectl top nodes
```

### View Events
```bash
kubectl get events -n url-shortener --sort-by='.lastTimestamp'
```

## 🔐 Security Notes

⚠️ **This is a development setup!**

- Default passwords are used (change in production)
- No TLS/SSL (use HTTPS in production)
- NodePort exposes services (use Ingress in production)
- No resource limits set (add in production)

## 📚 Next Steps

1. Wait for all pods to be `Running`
2. Configure Keycloak realm and client
3. Update `application-dev.yml` with Keycloak client secret
4. Run the application with `quarkus:dev`
5. Access Swagger UI: http://localhost:8080/q/swagger-ui

## 🐛 Common Issues

### Issue: Pods stuck in Pending
**Solution:** Check if Docker Desktop has enough resources (Settings → Resources)

### Issue: PVC not binding
**Solution:** Check storage class: `kubectl get storageclass`

### Issue: Cannot connect to services
**Solution:** Verify NodePort services: `kubectl get svc -n url-shortener`

### Issue: Keycloak not starting
**Solution:** Check logs: `kubectl logs -f deployment/keycloak -n url-shortener`

---

**Need help?** Run `./check-k8s-status.sh` or `.\check-k8s-status.ps1` to see detailed status.
