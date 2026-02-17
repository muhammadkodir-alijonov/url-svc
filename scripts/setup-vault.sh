#!/bin/bash

# Vault Setup Script for URL Shortener
# This script initializes Vault with all necessary secrets

# Function to pause before exit
pause_before_exit() {
    echo ""
    read -p "Press ENTER to exit..." dummy
}

# Always pause before exiting
trap pause_before_exit EXIT

VAULT_ADDR="http://localhost:30200"
VAULT_TOKEN="dev-root-token"
VAULT_ADDR_INTERNAL="http://127.0.0.1:8200"  # Vault's internal port inside the pod
NAMESPACE="url-shortener"

echo "🔐 Setting up Vault for URL Shortener Service"
echo "=============================================="

# Find the Vault pod
echo "🔍 Finding Vault pod..."
VAULT_POD=$(kubectl get pod -n $NAMESPACE -l app=vault -o jsonpath='{.items[0].metadata.name}' 2>&1)

if [ -z "$VAULT_POD" ] || [ "$VAULT_POD" == "error"* ]; then
    echo "❌ ERROR: Vault pod not found!"
    echo "   Make sure Vault is deployed: kubectl get pods -n $NAMESPACE -l app=vault"
    echo "   Error: $VAULT_POD"
    exit 1
fi

echo "✅ Found Vault pod: $VAULT_POD"

# Function to run vault commands in the pod
vault_exec() {
    echo "   Running: vault $@"
    kubectl exec -n $NAMESPACE $VAULT_POD -- env VAULT_ADDR="$VAULT_ADDR_INTERNAL" VAULT_TOKEN="$VAULT_TOKEN" vault "$@"
    if [ $? -ne 0 ]; then
        echo "   ❌ Command failed!"
        return 1
    fi
    return 0
}

# Export Vault environment variables
export VAULT_ADDR
export VAULT_TOKEN

# Wait for Vault pod to be ready
echo "⏳ Waiting for Vault pod to be ready..."
timeout=60
elapsed=0
while true; do
    POD_STATUS=$(kubectl get pod -n $NAMESPACE $VAULT_POD -o jsonpath='{.status.phase}' 2>/dev/null || echo "Unknown")
    if [ "$POD_STATUS" = "Running" ]; then
        break
    fi
    if [ $elapsed -ge $timeout ]; then
        echo "❌ Timeout waiting for Vault pod to be ready"
        echo "   Pod status: $POD_STATUS"
        echo "   Check with: kubectl get pods -n $NAMESPACE -l app=vault"
        exit 1
    fi
    sleep 2
    elapsed=$((elapsed + 2))
    echo "   Still waiting... (${elapsed}s) - Status: $POD_STATUS"
done

echo "✅ Vault pod is ready!"

# Enable KV secrets engine v2
echo ""
echo "📝 Enabling KV secrets engine v2..."
vault_exec secrets enable -version=2 -path=secret kv 2>/dev/null || echo "   KV engine already enabled (or failed - continuing anyway)"

# Store Database Secrets
echo ""
echo "🗄️  Storing Database secrets..."
vault_exec kv put secret/url-shortener/database/postgres \
    username="admin" \
    password="admin123" \
    host="localhost" \
    port="30432" \
    database="url_shortener" \
    jdbc_url="jdbc:postgresql://localhost:30432/url_shortener"

# Store Keycloak Secrets
echo ""
echo "🔑 Storing Keycloak secrets..."
vault_exec kv put secret/url-shortener/keycloak/config \
    server_url="http://localhost:30180" \
    realm="url-shortener" \
    client_id="url-shortener-client" \
    admin_username="admin" \
    admin_password="admin" \
    auth_server_url="http://localhost:30180/realms/url-shortener" \
    certs_url="http://localhost:30180/realms/url-shortener/protocol/openid-connect/certs"

# Store Redis Secrets
echo ""
echo "📮 Storing Redis secrets..."
vault_exec kv put secret/url-shortener/redis/config \
    host="localhost" \
    port="30379" \
    url="redis://localhost:30379"

# Store Pulsar Secrets
echo ""
echo "📡 Storing Pulsar secrets..."
vault_exec kv put secret/url-shortener/pulsar/config \
    broker_url="pulsar://localhost:30650" \
    admin_url="http://localhost:30081" \
    topic="url-shortener-clicks"

# Store APISIX Secrets
echo ""
echo "🚪 Storing APISIX secrets..."
vault_exec kv put secret/url-shortener/apisix/config \
    gateway_url="http://localhost:30900" \
    admin_url="http://localhost:30901" \
    dashboard_url="http://localhost:30910" \
    admin_key="admin-api-key" \
    dashboard_username="admin" \
    dashboard_password="admin"

# Store Vault Secrets
echo ""
echo "🔐 Storing Vault secrets..."
vault_exec kv put secret/url-shortener/vault/config \
    url="http://localhost:30200" \
    token="dev-root-token"

# Store Application Secrets
echo ""
echo "⚙️  Storing Application secrets..."
vault_exec kv put secret/url-shortener/application/config \
    base_url="http://localhost:3000" \
    short_code_length="7" \
    short_code_max_attempts="10" \
    cache_url_ttl="3600" \
    rate_limit_shorten="100" \
    rate_limit_redirect="1000"

# Verify secrets
echo ""
echo "✅ Verifying secrets..."
echo ""

echo "Database secrets:"
vault_exec kv get secret/url-shortener/database/postgres

echo ""
echo "Keycloak secrets:"
vault_exec kv get secret/url-shortener/keycloak/config

echo ""
echo "Redis secrets:"
vault_exec kv get secret/url-shortener/redis/config

echo ""
echo "Pulsar secrets:"
vault_exec kv get secret/url-shortener/pulsar/config

echo ""
echo "APISIX secrets:"
vault_exec kv get secret/url-shortener/apisix/config

echo ""
echo "=============================================="
echo "✅ Vault setup completed successfully!"
echo ""
echo "You can now access secrets using:"
echo "  kubectl exec -n url-shortener $VAULT_POD -- vault kv get secret/url-shortener/<path>"
echo ""
echo "Or via the application using VaultService"
echo "=============================================="
