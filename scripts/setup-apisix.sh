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

# 1. Create Upstream for URL Service (localhost:8080)
echo ""
echo "📡 Creating upstream for URL Service..."
curl -s "${APISIX_ADMIN}/apisix/admin/upstreams/10" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "url",
  "type": "roundrobin",
  "nodes": {
    "host.docker.internal:8080": 1
  }
}' | jq .

# 2. Route for /q/swagger-index (no auth, direct to upstream)
echo ""
echo "🔗 Creating /q/swagger-index route..."
curl -s "${APISIX_ADMIN}/apisix/admin/routes/10" \
  -H "X-API-KEY: ${API_KEY}" \
  -X PUT -d '{
  "name": "swagger-index",
  "uri": "/q/swagger-index",
  "methods": ["GET"],
  "upstream_id": 10
}' | jq .

# Verify route
echo ""
echo "=============================================="
echo "✅ APISIX Route Configured!"
echo "=============================================="

echo ""
echo "📋 Configured Route:"
curl -s "${APISIX_ADMIN}/apisix/admin/routes/10" \
  -H "X-API-KEY: ${API_KEY}" | jq '{id: .value.id, name: .value.name, uri: .value.uri}'

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
