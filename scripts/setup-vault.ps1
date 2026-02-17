# Vault Setup Script for URL Shortener (Windows PowerShell)
# This script initializes Vault with all necessary secrets

$ErrorActionPreference = "Stop"

$VAULT_ADDR = "http://localhost:30200"
$VAULT_TOKEN = "dev-root-token"

Write-Host "🔐 Setting up Vault for URL Shortener Service" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

# Set environment variables
$env:VAULT_ADDR = $VAULT_ADDR
$env:VAULT_TOKEN = $VAULT_TOKEN

# Wait for Vault to be ready
Write-Host "`n⏳ Waiting for Vault to be ready..." -ForegroundColor Yellow
$timeout = 60
$elapsed = 0

while ($elapsed -lt $timeout) {
    try {
        $response = Invoke-WebRequest -Uri "$VAULT_ADDR/v1/sys/health" -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            break
        }
    } catch {
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host "   Still waiting... (${elapsed}s)" -ForegroundColor Gray
    }
}

if ($elapsed -ge $timeout) {
    Write-Host "❌ Timeout waiting for Vault to be ready" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Vault is ready!" -ForegroundColor Green

# Enable KV secrets engine v2
Write-Host "`n📝 Enabling KV secrets engine v2..." -ForegroundColor Yellow
vault secrets enable -version=2 -path=secret kv 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "   KV engine already enabled" -ForegroundColor Gray
}

# Store Database Secrets
Write-Host "`n🗄️  Storing Database secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/database/postgres `
    username="admin" `
    password="admin123" `
    host="localhost" `
    port="30432" `
    database="url_shortener" `
    jdbc_url="jdbc:postgresql://localhost:30432/url_shortener"

# Store Keycloak Secrets
Write-Host "`n🔑 Storing Keycloak secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/keycloak/config `
    server_url="http://localhost:30180" `
    realm="url-shortener" `
    client_id="url-shortener-client" `
    admin_username="admin" `
    admin_password="admin" `
    auth_server_url="http://localhost:30180/realms/url-shortener" `
    certs_url="http://localhost:30180/realms/url-shortener/protocol/openid-connect/certs"

# Store Redis Secrets
Write-Host "`n📮 Storing Redis secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/redis/config `
    host="localhost" `
    port="30379" `
    url="redis://localhost:30379"

# Store Pulsar Secrets
Write-Host "`n📡 Storing Pulsar secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/pulsar/config `
    broker_url="pulsar://localhost:30650" `
    admin_url="http://localhost:30081" `
    topic="url-shortener-clicks"

# Store APISIX Secrets
Write-Host "`n🚪 Storing APISIX secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/apisix/config `
    gateway_url="http://localhost:30900" `
    admin_url="http://localhost:30901" `
    dashboard_url="http://localhost:30910" `
    admin_key="admin-api-key" `
    dashboard_username="admin" `
    dashboard_password="admin"

# Store Vault Secrets
Write-Host "`n🔐 Storing Vault secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/vault/config `
    url="http://localhost:30200" `
    token="dev-root-token"

# Store Application Secrets
Write-Host "`n⚙️  Storing Application secrets..." -ForegroundColor Yellow
vault kv put secret/url-shortener/application/config `
    base_url="http://localhost:3000" `
    short_code_length="7" `
    short_code_max_attempts="10" `
    cache_url_ttl="3600" `
    rate_limit_shorten="100" `
    rate_limit_redirect="1000"

# Verify secrets
Write-Host "`n✅ Verifying secrets..." -ForegroundColor Green
Write-Host ""

Write-Host "Database secrets:" -ForegroundColor Cyan
vault kv get secret/url-shortener/database/postgres

Write-Host "`nKeycloak secrets:" -ForegroundColor Cyan
vault kv get secret/url-shortener/keycloak/config

Write-Host "`nRedis secrets:" -ForegroundColor Cyan
vault kv get secret/url-shortener/redis/config

Write-Host "`nPulsar secrets:" -ForegroundColor Cyan
vault kv get secret/url-shortener/pulsar/config

Write-Host "`nAPISIX secrets:" -ForegroundColor Cyan
vault kv get secret/url-shortener/apisix/config

Write-Host "`n==============================================" -ForegroundColor Cyan
Write-Host "✅ Vault setup completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "You can now access secrets using:" -ForegroundColor White
Write-Host "  vault kv get secret/url-shortener/<path>" -ForegroundColor Gray
Write-Host ""
Write-Host "Or via the application using VaultService" -ForegroundColor White
Write-Host "==============================================" -ForegroundColor Cyan

