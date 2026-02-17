#!/bin/bash

# Vault Setup Script for URL Shortener
# This script initializes Vault with all necessary secrets

set -e

VAULT_ADDR="http://localhost:30200"
VAULT_TOKEN="dev-root-token"

echo "🔐 Setting up Vault for URL Shortener Service"
echo "=============================================="

# Export Vault environment variables
export VAULT_ADDR
export VAULT_TOKEN

# Wait for Vault to be ready
echo "⏳ Waiting for Vault to be ready..."
timeout=60
elapsed=0
while ! curl -s -f "$VAULT_ADDR/v1/sys/health" > /dev/null 2>&1; do
    if [ $elapsed -ge $timeout ]; then
        echo "❌ Timeout waiting for Vault to be ready"
        exit 1
    fi
    sleep 2
    elapsed=$((elapsed + 2))
    echo "   Still waiting... (${elapsed}s)"
done

echo "✅ Vault is ready!"

# Enable KV secrets engine v2
echo ""
echo "📝 Enabling KV secrets engine v2..."
vault secrets enable -version=2 -path=secret kv 2>/dev/null || echo "   KV engine already enabled"

# Store Database Secrets
echo ""
echo "🗄️  Storing Database secrets..."
vault kv put secret/url-shortener/database/postgres \
    username="admin" \
    password="admin123" \
    host="localhost" \
    port="30432" \
    database="url_shortener" \
    jdbc_url="jdbc:postgresql://localhost:30432/url_shortener"

# Store Keycloak Secrets
echo ""
echo "🔑 Storing Keycloak secrets..."
vault kv put secret/url-shortener/keycloak/config \
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
vault kv put secret/url-shortener/redis/config \
    host="localhost" \
    port="30379" \
    url="redis://localhost:30379"

# Store Pulsar Secrets
echo ""
echo "📡 Storing Pulsar secrets..."
vault kv put secret/url-shortener/pulsar/config \
    broker_url="pulsar://localhost:30650" \
    admin_url="http://localhost:30081" \
    topic="url-shortener-clicks"

# Store APISIX Secrets
echo ""
echo "🚪 Storing APISIX secrets..."
vault kv put secret/url-shortener/apisix/config \
    gateway_url="http://localhost:30900" \
    admin_url="http://localhost:30901" \
    dashboard_url="http://localhost:30910" \
    admin_key="admin-api-key" \
    dashboard_username="admin" \
    dashboard_password="admin"

# Store Vault Secrets
echo ""
echo "🔐 Storing Vault secrets..."
vault kv put secret/url-shortener/vault/config \
    url="http://localhost:30200" \
    token="dev-root-token"

# Store Application Secrets
echo ""
echo "⚙️  Storing Application secrets..."
vault kv put secret/url-shortener/application/config \
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
vault kv get secret/url-shortener/database/postgres

echo ""
echo "Keycloak secrets:"
vault kv get secret/url-shortener/keycloak/config

echo ""
echo "Redis secrets:"
vault kv get secret/url-shortener/redis/config

echo ""
echo "Pulsar secrets:"
vault kv get secret/url-shortener/pulsar/config

echo ""
echo "APISIX secrets:"
vault kv get secret/url-shortener/apisix/config

echo ""
echo "=============================================="
echo "✅ Vault setup completed successfully!"
echo ""
echo "You can now access secrets using:"
echo "  vault kv get secret/url-shortener/<path>"
echo ""
echo "Or via the application using VaultService"
echo "=============================================="

