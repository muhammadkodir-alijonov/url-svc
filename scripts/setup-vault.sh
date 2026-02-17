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

# Function to store secrets for a specific environment
store_secrets() {
    local ENV=$1
    local DB_HOST=$2
    local DB_PORT=$3
    local DB_PASSWORD=$4
    local REDIS_HOST=$5
    local REDIS_PORT=$6
    local KEYCLOAK_URL=$7
    local PULSAR_BROKER=$8
    local PULSAR_ADMIN=$9
    local APISIX_GATEWAY=${10}
    local VAULT_URL=${11}
    local BASE_URL=${12}

    echo ""
    echo "=============================================="
    echo "Setting up secrets for: $ENV environment"
    echo "=============================================="

    # Store Database Secrets
    echo ""
    echo "🗄️  Storing Database secrets..."
    vault_exec kv put secret/url-shortener/$ENV/database/postgres \
        username="admin" \
        password="$DB_PASSWORD" \
        host="$DB_HOST" \
        port="$DB_PORT" \
        database="url_shortener" \
        jdbc_url="jdbc:postgresql://$DB_HOST:$DB_PORT/url_shortener"

    # Store Keycloak Secrets
    echo ""
    echo "🔑 Storing Keycloak secrets..."
    vault_exec kv put secret/url-shortener/$ENV/keycloak/config \
        server_url="$KEYCLOAK_URL" \
        realm="url-shortener" \
        client_id="url-shortener-client" \
        admin_username="admin" \
        admin_password="admin" \
        auth_server_url="$KEYCLOAK_URL/realms/url-shortener" \
        certs_url="$KEYCLOAK_URL/realms/url-shortener/protocol/openid-connect/certs"

    # Store Redis Secrets
    echo ""
    echo "📮 Storing Redis secrets..."
    vault_exec kv put secret/url-shortener/$ENV/redis/config \
        host="$REDIS_HOST" \
        port="$REDIS_PORT" \
        url="redis://$REDIS_HOST:$REDIS_PORT"

    # Store Pulsar Secrets
    echo ""
    echo "📡 Storing Pulsar secrets..."
    vault_exec kv put secret/url-shortener/$ENV/pulsar/config \
        broker_url="$PULSAR_BROKER" \
        admin_url="$PULSAR_ADMIN" \
        topic="url-shortener-clicks"

    # Store APISIX Secrets
    echo ""
    echo "🚪 Storing APISIX secrets..."
    vault_exec kv put secret/url-shortener/$ENV/apisix/config \
        gateway_url="$APISIX_GATEWAY" \
        admin_url="http://localhost:30901" \
        dashboard_url="http://localhost:30910" \
        admin_key="admin-api-key" \
        dashboard_username="admin" \
        dashboard_password="admin"

    # Store Vault Secrets
    echo ""
    echo "🔐 Storing Vault secrets..."
    vault_exec kv put secret/url-shortener/$ENV/vault/config \
        url="$VAULT_URL" \
        token="dev-root-token"

    # Store Application Secrets
    echo ""
    echo "⚙️  Storing Application secrets..."
    vault_exec kv put secret/url-shortener/$ENV/application/config \
        base_url="$BASE_URL" \
        short_code_length="7" \
        short_code_max_attempts="10" \
        cache_url_ttl="3600" \
        rate_limit_shorten="100" \
        rate_limit_redirect="1000"

    echo ""
    echo "✅ $ENV secrets stored successfully!"
}

# Store DEV environment secrets
store_secrets "dev" \
    "localhost" "30432" "admin123" \
    "localhost" "30379" \
    "http://localhost:30180" \
    "pulsar://localhost:30650" "http://localhost:30081" \
    "http://localhost:30900" \
    "http://localhost:30200" \
    "http://localhost:3000"

# Store PROD environment secrets (with production values)
store_secrets "prod" \
    "postgres.url-shortener.svc.cluster.local" "5432" "ProductionP@ssw0rd!" \
    "valkey.url-shortener.svc.cluster.local" "6379" \
    "http://keycloak.url-shortener.svc.cluster.local:8080" \
    "pulsar://pulsar.url-shortener.svc.cluster.local:6650" "http://pulsar.url-shortener.svc.cluster.local:8080" \
    "http://apisix-gateway.url-shortener.svc.cluster.local:9080" \
    "http://vault.url-shortener.svc.cluster.local:8200" \
    "https://short.yourdomain.com"

# Verify secrets
echo ""
echo "=============================================="
echo "✅ Verifying DEV secrets..."
echo "=============================================="

echo ""
echo "DEV - Database secrets:"
vault_exec kv get secret/url-shortener/dev/database/postgres

echo ""
echo "DEV - Redis secrets:"
vault_exec kv get secret/url-shortener/dev/redis/config

echo ""
echo "=============================================="
echo "✅ Verifying PROD secrets..."
echo "=============================================="

echo ""
echo "PROD - Database secrets:"
vault_exec kv get secret/url-shortener/prod/database/postgres

echo ""
echo "PROD - Redis secrets:"
vault_exec kv get secret/url-shortener/prod/redis/config

echo ""
echo "=============================================="
echo "✅ Vault setup completed successfully!"
echo ""
echo "Secrets are organized by environment:"
echo ""
echo "  DEV:  secret/url-shortener/dev/<service>/config"
echo "  PROD: secret/url-shortener/prod/<service>/config"
echo ""
echo "Access secrets using:"
echo "  kubectl exec -n url-shortener $VAULT_POD -- vault kv get secret/url-shortener/dev/<path>"
echo "  kubectl exec -n url-shortener $VAULT_POD -- vault kv get secret/url-shortener/prod/<path>"
echo ""
echo "Or via the application using VaultService with environment prefix"
echo "=============================================="
