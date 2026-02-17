# APISIX Gateway Guide

## Overview

APISIX is our API Gateway that handles:
- **Routing**: Route requests to backend services
- **Load Balancing**: Distribute traffic across instances
- **Rate Limiting**: Protect against abuse
- **Authentication**: JWT validation, API keys
- **Monitoring**: Metrics and logging
- **Circuit Breaking**: Fault tolerance

## Architecture

```
Client Request
      ↓
APISIX Gateway (Port 30900)
      ↓
Backend Services (URL Shortener API)
```

## Components

### 1. APISIX Gateway (Port 30900)
**Purpose:** Main API Gateway - handles all incoming HTTP traffic

**Access:**
```bash
curl http://localhost:30900/
```

**Use Cases:**
- Client-facing API endpoints
- Load balancing across service instances
- Rate limiting and throttling
- Request/response transformation
- Circuit breaking

### 2. APISIX Admin API (Port 30901)
**Purpose:** REST API for configuring APISIX programmatically

**Access:**
```bash
curl http://localhost:30901/apisix/admin/routes \
  -H 'X-API-KEY: admin-api-key'
```

**Use Cases:**
- Create/update routes dynamically
- Configure plugins
- Manage upstreams
- Automation and CI/CD

### 3. APISIX Dashboard (Port 30910)
**Purpose:** Web UI for visual management

**Access:**
- URL: `http://localhost:30910`
- Username: `admin`
- Password: `admin`

**Use Cases:**
- Visual route configuration
- Monitor traffic and metrics
- Debug issues
- Test configurations

## Getting Started

### 1. Access APISIX Dashboard

1. Open browser: `http://localhost:30910`
2. Login with:
   - Username: `admin`
   - Password: `admin`

### 2. Create a Route

**Using Dashboard:**
1. Go to Routes → Create
2. Configure:
   - Name: `url-shortener-api`
   - Path: `/api/*`
   - Upstream: `http://localhost:8080`
3. Click Save

**Using Admin API:**
```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PUT -d '
{
  "uri": "/api/*",
  "name": "url-shortener-api",
  "methods": ["GET", "POST", "PUT", "DELETE"],
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "localhost:8080": 1
    }
  }
}'
```

### 3. Test the Route

```bash
# Request through APISIX Gateway
curl http://localhost:30900/api/health

# Direct request (bypass gateway)
curl http://localhost:8080/api/health
```

## Common Use Cases

### Rate Limiting

Protect your API from abuse:

```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PATCH -d '
{
  "plugins": {
    "limit-req": {
      "rate": 100,
      "burst": 50,
      "key": "remote_addr",
      "rejected_code": 429
    }
  }
}'
```

### JWT Authentication

Validate JWT tokens:

```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PATCH -d '
{
  "plugins": {
    "jwt-auth": {
      "key": "user-key",
      "secret": "my-secret-key",
      "algorithm": "RS256"
    }
  }
}'
```

### CORS Configuration

Enable CORS for frontend:

```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PATCH -d '
{
  "plugins": {
    "cors": {
      "allow_origins": "http://localhost:3000",
      "allow_methods": "GET,POST,PUT,DELETE,OPTIONS",
      "allow_headers": "Authorization,Content-Type",
      "max_age": 3600
    }
  }
}'
```

### Load Balancing

Distribute traffic across multiple instances:

```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PATCH -d '
{
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "localhost:8080": 1,
      "localhost:8081": 1,
      "localhost:8082": 1
    },
    "checks": {
      "active": {
        "healthy": {
          "interval": 5,
          "successes": 2
        },
        "unhealthy": {
          "interval": 5,
          "http_failures": 3
        }
      }
    }
  }
}'
```

### Circuit Breaker

Prevent cascading failures:

```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PATCH -d '
{
  "plugins": {
    "api-breaker": {
      "break_response_code": 503,
      "unhealthy": {
        "http_statuses": [500, 503],
        "failures": 3
      },
      "healthy": {
        "http_statuses": [200],
        "successes": 2
      }
    }
  }
}'
```

## Admin API Reference

### Routes

**List Routes:**
```bash
curl http://localhost:30901/apisix/admin/routes \
  -H 'X-API-KEY: admin-api-key'
```

**Get Route:**
```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key'
```

**Create Route:**
```bash
curl http://localhost:30901/apisix/admin/routes \
  -H 'X-API-KEY: admin-api-key' \
  -X POST -d '{...}'
```

**Update Route:**
```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PUT -d '{...}'
```

**Delete Route:**
```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X DELETE
```

### Upstreams

**List Upstreams:**
```bash
curl http://localhost:30901/apisix/admin/upstreams \
  -H 'X-API-KEY: admin-api-key'
```

**Create Upstream:**
```bash
curl http://localhost:30901/apisix/admin/upstreams/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PUT -d '
{
  "type": "roundrobin",
  "nodes": {
    "localhost:8080": 1
  }
}'
```

### Services

**List Services:**
```bash
curl http://localhost:30901/apisix/admin/services \
  -H 'X-API-KEY: admin-api-key'
```

**Create Service:**
```bash
curl http://localhost:30901/apisix/admin/services/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PUT -d '
{
  "upstream": {
    "type": "roundrobin",
    "nodes": {
      "localhost:8080": 1
    }
  }
}'
```

## Monitoring

### Prometheus Metrics

APISIX exposes Prometheus metrics:

**Access:**
```bash
curl http://localhost:30902/apisix/prometheus/metrics
```

**Common Metrics:**
- `apisix_http_requests_total` - Total requests
- `apisix_http_latency` - Request latency
- `apisix_bandwidth` - Bandwidth usage
- `apisix_upstream_status` - Upstream health

### Logging

Enable request logging:

```bash
curl http://localhost:30901/apisix/admin/routes/1 \
  -H 'X-API-KEY: admin-api-key' \
  -X PATCH -d '
{
  "plugins": {
    "http-logger": {
      "uri": "http://localhost:9200/apisix/logs",
      "batch_max_size": 100,
      "max_retry_count": 3
    }
  }
}'
```

## Troubleshooting

### Check APISIX Status

```bash
# Gateway health
curl http://localhost:30900/apisix/status

# Admin API
curl http://localhost:30901/apisix/admin/routes \
  -H 'X-API-KEY: admin-api-key'

# Pod logs
kubectl logs -n url-shortener -l app=apisix
```

### Common Issues

**1. Route Not Working**
```bash
# Verify route exists
curl http://localhost:30901/apisix/admin/routes \
  -H 'X-API-KEY: admin-api-key'

# Check upstream health
curl http://localhost:30901/apisix/admin/upstreams \
  -H 'X-API-KEY: admin-api-key'
```

**2. 401 Unauthorized**
```bash
# Verify API key
echo $APISIX_API_KEY

# Use correct key
curl http://localhost:30901/apisix/admin/routes \
  -H 'X-API-KEY: admin-api-key'
```

**3. Dashboard Not Accessible**
```bash
# Check pod status
kubectl get pods -n url-shortener -l app=apisix-dashboard

# Check logs
kubectl logs -n url-shortener -l app=apisix-dashboard
```

## Best Practices

### 1. Security
- ✅ Change default admin password
- ✅ Use strong API keys
- ✅ Enable HTTPS in production
- ✅ Implement authentication on all routes
- ✅ Use rate limiting

### 2. Performance
- ✅ Enable caching where appropriate
- ✅ Configure health checks
- ✅ Use connection pooling
- ✅ Set appropriate timeouts
- ✅ Monitor metrics

### 3. Reliability
- ✅ Implement circuit breakers
- ✅ Configure retries
- ✅ Use load balancing
- ✅ Set up monitoring
- ✅ Regular backups of configuration

## Integration with Application

### Update Application to Use Gateway

Instead of direct access:
```
http://localhost:8080/api/urls
```

Use through gateway:
```
http://localhost:30900/api/urls
```

### Frontend Configuration

Update frontend to point to gateway:

```javascript
// .env or config
API_BASE_URL=http://localhost:30900
```

## Additional Resources

- [APISIX Documentation](https://apisix.apache.org/docs/)
- [APISIX Plugins](https://apisix.apache.org/docs/apisix/plugins/batch-requests/)
- [Admin API Reference](https://apisix.apache.org/docs/apisix/admin-api/)

