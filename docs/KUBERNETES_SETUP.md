# ☸️ Kubernetes Development Guide

## 📋 Overview

This guide shows how to run **infrastructure in Kubernetes** (Docker Desktop) while developing your **application locally** with hot reload.

```
┌──────────────────────────────────────┐
│  Local Machine (Windows)             │
│                                      │
│  ┌────────────────────────────────┐ │
│  │  Quarkus Application (Local)   │ │
│  │  Port: 8080                    │ │
│  │  Hot Reload: ✅                │ │
│  └────────────────────────────────┘ │
│              ↓                       │
│  ┌────────────────────────────────┐ │
│  │  Docker Desktop Kubernetes     │ │
│  │                                │ │
│  │  🐘 PostgreSQL  → :30432       │ │
│  │  🔴 Valkey      → :30379       │ │
│  │  📨 Pulsar      → :30650       │ │
│  │  🔐 Keycloak    → :30180       │ │
│  └────────────────────────────────┘ │
└──────────────────────────────────────┘
```

---

## 🚀 Quick Start (4 Steps)

### 1️⃣ Deploy Infrastructure to Kubernetes
```powershell
.\deploy-k8s.ps1
```

### 2️⃣ Wait for Pods to be Ready
```powershell
kubectl get pods -n url-shortener -w
# Press Ctrl+C when all pods are Running
```

### 3️⃣ Verify Infrastructure
```powershell
.\check-k8s-status.ps1
```

### 4️⃣ Start Application
```powershell
.\start-dev.ps1
```

**Done!** 🎉 Open http://localhost:8080/swagger-ui

---

## 📦 What Gets Deployed to Kubernetes?

### Namespace
- `url-shortener` - Isolates all resources

### StatefulSets (with Persistent Storage)
- **PostgreSQL** - Database with 5GB volume
- **Valkey** - Redis cache with 1GB volume
- **Pulsar** - Message broker with 5GB volume

### Deployments
- **Keycloak** - Authentication server

### Services (NodePort)
- PostgreSQL: `localhost:30432`
- Valkey: `localhost:30379`
- Pulsar: `localhost:30650`
- Pulsar Admin: `localhost:30081`
- Keycloak: `localhost:30180`

---

## 🔧 Manual Deployment Steps

### Step 1: Enable Kubernetes in Docker Desktop

1. Open **Docker Desktop**
2. Go to **Settings** → **Kubernetes**
3. Check **Enable Kubernetes**
4. Click **Apply & Restart**
5. Wait for Kubernetes to start

Verify:
```powershell
kubectl version
kubectl cluster-info
```

### Step 2: Deploy Namespace

```powershell
kubectl apply -f infrastructure/kubernetes/namespace.yaml
```

Verify:
```powershell
kubectl get namespace url-shortener
```

### Step 3: Deploy PostgreSQL

```powershell
kubectl apply -f infrastructure/kubernetes/postgres-statefulset.yaml
```

This creates:
- ConfigMap with DB credentials
- Service (NodePort 30432)
- StatefulSet with persistent volume

Wait for ready:
```powershell
kubectl wait --for=condition=ready pod -l app=postgres -n url-shortener --timeout=120s
```

### Step 4: Deploy Valkey (Redis)

```powershell
kubectl apply -f infrastructure/kubernetes/valkey-statefulset.yaml
```

Wait for ready:
```powershell
kubectl wait --for=condition=ready pod -l app=valkey -n url-shortener --timeout=120s
```

### Step 5: Deploy Pulsar

```powershell
kubectl apply -f infrastructure/kubernetes/pulsar-statefulset.yaml
```

⚠️ **Note**: Pulsar takes 1-2 minutes to start.

Wait for ready:
```powershell
kubectl wait --for=condition=ready pod -l app=pulsar -n url-shortener --timeout=180s
```

### Step 6: Deploy Keycloak

```powershell
kubectl apply -f infrastructure/kubernetes/keycloak-deployment.yaml
```

⚠️ **Note**: Keycloak depends on PostgreSQL and takes 1-2 minutes to start.

Wait for ready:
```powershell
kubectl wait --for=condition=ready pod -l app=keycloak -n url-shortener --timeout=180s
```

---

## 🔍 Verify Deployment

### Check All Resources
```powershell
kubectl get all -n url-shortener
```

### Check Pods
```powershell
kubectl get pods -n url-shortener
```

Expected output:
```
NAME          READY   STATUS    RESTARTS   AGE
postgres-0    1/1     Running   0          2m
valkey-0      1/1     Running   0          2m
pulsar-0      1/1     Running   0          2m
keycloak-xxx  1/1     Running   0          2m
```

### Check Services
```powershell
kubectl get services -n url-shortener
```

Expected output:
```
NAME       TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
postgres   NodePort   10.x.x.x       <none>        5432:30432/TCP   2m
valkey     NodePort   10.x.x.x       <none>        6379:30379/TCP   2m
pulsar     NodePort   10.x.x.x       <none>        6650:30650/TCP   2m
keycloak   NodePort   10.x.x.x       <none>        8080:30180/TCP   2m
```

### Check Storage
```powershell
kubectl get pvc -n url-shortener
```

Expected output:
```
NAME                     STATUS   VOLUME    CAPACITY   ACCESS MODES
postgres-storage-...     Bound    pvc-xxx   5Gi        RWO
valkey-storage-...       Bound    pvc-xxx   1Gi        RWO
pulsar-storage-...       Bound    pvc-xxx   5Gi        RWO
```

---

## 🧪 Test Infrastructure

### Test PostgreSQL
```powershell
# Get pod name
$pgPod = kubectl get pod -n url-shortener -l app=postgres -o jsonpath='{.items[0].metadata.name}'

# Test connection
kubectl exec -it $pgPod -n url-shortener -- psql -U admin -d url_shortener -c "SELECT version();"
```

### Test Valkey
```powershell
# Get pod name
$valkeyPod = kubectl get pod -n url-shortener -l app=valkey -o jsonpath='{.items[0].metadata.name}'

# Test connection
kubectl exec -it $valkeyPod -n url-shortener -- valkey-cli ping
```

### Test Pulsar
```powershell
# Check Pulsar admin
curl http://localhost:30081/admin/v2/clusters
```

### Test Keycloak
```powershell
# Open in browser
start http://localhost:30180
# Login: admin / admin123
```

---

## 🔄 Update Configuration

Your `application-dev.yml` is already configured for Kubernetes NodePorts:

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

---

## 🚀 Start Development

```powershell
.\start-dev.ps1
```

or manually:
```powershell
.\mvnw.cmd quarkus:dev
```

---

## 📊 Monitoring & Debugging

### View Logs

**PostgreSQL:**
```powershell
kubectl logs -f -n url-shortener -l app=postgres
```

**Valkey:**
```powershell
kubectl logs -f -n url-shortener -l app=valkey
```

**Pulsar:**
```powershell
kubectl logs -f -n url-shortener -l app=pulsar
```

**Keycloak:**
```powershell
kubectl logs -f -n url-shortener -l app=keycloak
```

### Describe Pod (for troubleshooting)
```powershell
kubectl describe pod <pod-name> -n url-shortener
```

### Shell into Pod
```powershell
kubectl exec -it <pod-name> -n url-shortener -- /bin/sh
```

### Port Forward (if NodePort not working)
```powershell
kubectl port-forward -n url-shortener svc/postgres 5432:5432
kubectl port-forward -n url-shortener svc/valkey 6379:6379
kubectl port-forward -n url-shortener svc/pulsar 6650:6650
kubectl port-forward -n url-shortener svc/keycloak 8080:8080
```

### Watch Resources
```powershell
kubectl get pods -n url-shortener -w
kubectl get events -n url-shortener -w
```

---

## 🔧 Common Operations

### Restart a Pod
```powershell
kubectl delete pod <pod-name> -n url-shortener
# StatefulSet/Deployment will recreate it automatically
```

### Scale Deployment
```powershell
kubectl scale deployment keycloak --replicas=2 -n url-shortener
```

### Update ConfigMap
```powershell
# Edit the YAML file
kubectl apply -f infrastructure/kubernetes/postgres-statefulset.yaml

# Restart pods to pick up changes
kubectl rollout restart statefulset postgres -n url-shortener
```

### Check Resource Usage
```powershell
kubectl top pods -n url-shortener
kubectl top nodes
```

---

## 🗑️ Cleanup

### Delete Everything
```powershell
.\delete-k8s.ps1
```

or manually:
```powershell
kubectl delete namespace url-shortener
```

⚠️ **Warning**: This deletes all data!

### Delete Specific Resource
```powershell
kubectl delete statefulset postgres -n url-shortener
kubectl delete deployment keycloak -n url-shortener
kubectl delete service postgres -n url-shortener
```

---

## 🐛 Troubleshooting

### Pods not starting?

**Check events:**
```powershell
kubectl get events -n url-shortener --sort-by='.lastTimestamp'
```

**Check pod status:**
```powershell
kubectl describe pod <pod-name> -n url-shortener
```

### Persistent Volume issues?

**Check PVCs:**
```powershell
kubectl get pvc -n url-shortener
kubectl describe pvc <pvc-name> -n url-shortener
```

**Check PVs:**
```powershell
kubectl get pv
```

### Can't connect from application?

**Verify services:**
```powershell
kubectl get services -n url-shortener
```

**Test from local machine:**
```powershell
curl http://localhost:30432
curl http://localhost:30379
curl http://localhost:30650
curl http://localhost:30180
```

**Check if ports are in use:**
```powershell
netstat -ano | findstr "30432"
netstat -ano | findstr "30379"
netstat -ano | findstr "30650"
netstat -ano | findstr "30180"
```

### Keycloak not starting?

Check if PostgreSQL is ready:
```powershell
kubectl get pod -n url-shortener -l app=postgres
```

View Keycloak logs:
```powershell
kubectl logs -f -n url-shortener -l app=keycloak
```

### Out of disk space?

Check Docker Desktop storage settings:
- Settings → Resources → Advanced → Disk image size

Clean up unused volumes:
```powershell
docker system prune -a --volumes
```

---

## 📚 Kubernetes Cheat Sheet

```powershell
# Get everything
kubectl get all -n url-shortener

# Get pods
kubectl get pods -n url-shortener
kubectl get pods -n url-shortener -o wide
kubectl get pods -n url-shortener -w

# Get services
kubectl get services -n url-shortener

# Get logs
kubectl logs <pod-name> -n url-shortener
kubectl logs -f <pod-name> -n url-shortener
kubectl logs --previous <pod-name> -n url-shortener

# Describe resources
kubectl describe pod <pod-name> -n url-shortener
kubectl describe service <service-name> -n url-shortener

# Execute commands
kubectl exec <pod-name> -n url-shortener -- <command>
kubectl exec -it <pod-name> -n url-shortener -- /bin/sh

# Port forwarding
kubectl port-forward <pod-name> -n url-shortener 8080:8080

# Delete resources
kubectl delete pod <pod-name> -n url-shortener
kubectl delete namespace url-shortener

# Apply configuration
kubectl apply -f <file.yaml>

# Watch events
kubectl get events -n url-shortener -w
```

---

## ✅ Development Workflow

1. **Start Infrastructure** (once):
   ```powershell
   .\deploy-k8s.ps1
   ```

2. **Verify Status**:
   ```powershell
   .\check-k8s-status.ps1
   ```

3. **Start Application** (daily):
   ```powershell
   .\start-dev.ps1
   ```

4. **Develop**:
   - Edit code
   - Save
   - Hot reload happens automatically
   - Test in Swagger UI

5. **Stop Application**:
   - Press `Ctrl+C` in terminal

6. **Infrastructure keeps running** until you delete it

7. **Cleanup** (when done):
   ```powershell
   .\delete-k8s.ps1
   ```

---

## 🎯 Benefits of Kubernetes Setup

✅ **Production-like Environment**
- Same tools as production
- Learn Kubernetes
- Test scaling and resilience

✅ **Persistent Data**
- Data survives pod restarts
- Can stop/start without data loss

✅ **Resource Management**
- CPU and memory limits
- Better than Docker Compose

✅ **Service Discovery**
- DNS-based service names
- Load balancing built-in

✅ **Easy Monitoring**
- Built-in health checks
- Resource metrics
- Event logs

---

## 📱 Access Points

| Service | Internal (K8s) | External (NodePort) | Purpose |
|---------|----------------|---------------------|---------|
| PostgreSQL | postgres:5432 | localhost:30432 | Database |
| Valkey | valkey:6379 | localhost:30379 | Cache |
| Pulsar Broker | pulsar:6650 | localhost:30650 | Messages |
| Pulsar Admin | pulsar:8080 | localhost:30081 | Admin UI |
| Keycloak | keycloak:8080 | localhost:30180 | Auth |

---

## 🎓 Next Steps

1. ✅ Deploy infrastructure to Kubernetes
2. ✅ Verify all pods are running
3. ✅ Start application locally
4. ✅ Test API with Swagger UI
5. 🚀 Start developing!

For production deployment, see: `docs/PRODUCTION_K8S.md`

---

**Happy Kubernetes Development! ☸️**
