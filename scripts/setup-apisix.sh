#!/bin/bash
# Setup APISIX Routes for URL Shortener

# Use port-forward: kubectl port-forward -n url-shorten <apisix-pod> 9180:9180
APISIX_ADMIN="http://localhost:9180"
# APISIX 3.7.0 default admin key (when using etcd mode)
API_KEY="edd1c9f034335f136f87ad84b625c8f1"

echo "🚀 Configuring APISIX Routes for URL Shortener"
echo "=============================================="

# Function to pause before exit
pause_before_exit() {
    echo ""
    read -p "Press ENTER to exit..." dummy
}

# Always pause before exiting
trap pause_before_exit EXIT

# Wait for APISIX to be ready
echo ""
echo "⏳ Waiting for APISIX to be ready..."
timeout=60
elapsed=0
while true; do
    response=$(curl -s -o /dev/null -w "%{http_code}" ${APISIX_ADMIN}/apisix/admin/routes -H "X-API-KEY: ${API_KEY}" 2>/dev/null)
    if [ "$response" = "200" ]; then
        echo "✅ APISIX is ready!"
        break
    fi
    if [ $elapsed -ge $timeout ]; then
        echo "❌ Timeout waiting for APISIX"
        exit 1
    fi
    sleep 2
    elapsed=$((elapsed + 2))
    echo "   Still waiting... (${elapsed}s) - Response: $response"
done

# 1. Create Upstream for URL Service
echo ""
echo "📡 Creating upstream for URL Service..."
curl -s "${APISIX_ADMIN}/apisix/admin/upstreams/1" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "url-service",
  "type": "roundrobin",
  "nodes": {
    "host.docker.internal:8080": 1
  },
  "timeout": {
    "connect": 6,
    "send": 6,
    "read": 6
  },
  "checks": {
    "active": {
      "type": "http",
      "http_path": "/q/health/live",
      "healthy": {
        "interval": 2,
        "successes": 2
      },
      "unhealthy": {
        "interval": 1,
        "http_failures": 2
      }
    }
  }
}' | jq .

# 2. Public Route - Redirect (No Auth)
echo ""
echo "🔗 Creating redirect route (public)..."
curl -s "${APISIX_ADMIN}/apisix/admin/routes/1" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "url-redirect",
  "uri": "/*",
  "priority": 1,
  "methods": ["GET"],
  "upstream_id": 1,
  "plugins": {
    "limit-req": {
      "rate": 1000,
      "burst": 500,
      "key": "remote_addr",
      "rejected_code": 429,
      "rejected_msg": "Too many requests"
    },
    "prometheus": {}
  }
}' | jq .

# 3. Auth Routes - Login/Register (No JWT)
echo ""
echo "🔐 Creating auth routes (public)..."
curl -s "${APISIX_ADMIN}/apisix/admin/routes/2" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "auth-endpoints",
  "uris": ["/api/auth/*"],
  "priority": 10,
  "methods": ["GET", "POST"],
  "upstream_id": 1,
  "plugins": {
    "limit-req": {
      "rate": 10,
      "burst": 5,
      "key": "remote_addr",
      "rejected_code": 429
    },
    "cors": {
      "allow_origins": "*",
      "allow_methods": "GET,POST,OPTIONS",
      "allow_headers": "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization",
      "max_age": 3600
    }
  }
}' | jq .

# 4. Protected API Routes (Require JWT)
echo ""
echo "🔒 Creating protected API routes (JWT required)..."
curl -s "${APISIX_ADMIN}/apisix/admin/routes/3" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "url-api-protected",
  "uri": "/api/urls*",
  "priority": 20,
  "methods": ["GET", "POST", "PUT", "DELETE"],
  "upstream_id": 1,
  "plugins": {
    "openid-connect": {
      "client_id": "url-shorten-client",
      "client_secret": "**my sec keyyyy**",
      "discovery": "http://keycloak.url-shorten.svc.cluster.local:8080/realms/url-shorten/.well-known/openid-configuration",
      "bearer_only": true,
      "realm": "url-shorten",
      "introspection_endpoint_auth_method": "client_secret_post"
    },
    "limit-req": {
      "rate": 100,
      "burst": 50,
      "key": "consumer_name",
      "rejected_code": 429
    },
    "cors": {
      "allow_origins": "*",
      "allow_methods": "GET,POST,PUT,DELETE,OPTIONS",
      "allow_headers": "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization",
      "max_age": 3600
    },
    "prometheus": {}
  }
}' | jq .

# 5. Health Check Routes (Public)
echo ""
echo "❤️ Creating health check routes..."
curl -s "${APISIX_ADMIN}/apisix/admin/routes/4" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "health-checks",
  "uris": ["/q/*", "/api/health"],
  "priority": 30,
  "methods": ["GET"],
  "upstream_id": 1,
  "plugins": {
    "prometheus": {}
  }
}' | jq .

# 6. User API Routes (JWT required)
echo ""
echo "👤 Creating user API routes..."
curl -s "${APISIX_ADMIN}/apisix/admin/routes/5" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "user-api",
  "uri": "/api/users*",
  "priority": 20,
  "methods": ["GET", "PUT", "DELETE"],
  "upstream_id": 1,
  "plugins": {
    "openid-connect": {
      "client_id": "url-shorten-client",
      "client_secret": "**my sec keyyyy**",
      "discovery": "http://keycloak.url-shorten.svc.cluster.local:8080/realms/url-shorten/.well-known/openid-configuration",
      "bearer_only": true,
      "realm": "url-shorten"
    },
    "limit-req": {
      "rate": 50,
      "burst": 20,
      "key": "consumer_name",
      "rejected_code": 429
    }
  }
}' | jq .

# Verify routes
echo ""
echo "=============================================="
echo "✅ APISIX Routes Configured!"
echo "=============================================="

echo ""
echo "📋 Configured Routes:"
curl -s "${APISIX_ADMIN}/apisix/admin/routes" \
  -H "X-API-KEY: ${API_KEY}" | jq '.list.[] | {id: .value.id, name: .value.name, uri: .value.uri, uris: .value.uris}'

echo ""
echo "=============================================="
echo "🧪 Test Routes:"
echo "=============================================="
echo ""
echo "  1. Health Check (Public):"
echo "     curl http://localhost:30900/q/health"
echo ""
echo "  2. Redirect (Public):"
echo "     curl -L http://localhost:30900/abc123"
echo ""
echo "  3. Login (Public):"
echo "     curl http://localhost:30900/api/auth/login"
echo ""
echo "  4. Create URL (Protected - JWT required):"
echo "     curl -H 'Authorization: Bearer <token>' http://localhost:30900/api/urls"
echo ""
echo "=============================================="
echo "📊 APISIX Dashboard:"
echo "   http://localhost:30910"
echo "   Username: admin"
echo "   Password: admin"
echo "=============================================="
