# 🛑 Delete All Kubernetes Resources

Write-Host "================================" -ForegroundColor Red
Write-Host "Delete Kubernetes Infrastructure" -ForegroundColor Red
Write-Host "================================" -ForegroundColor Red

Write-Host "`n⚠️  WARNING: This will delete all resources in url-shortener namespace!" -ForegroundColor Yellow
Write-Host "   Including all data in PostgreSQL, Valkey, and Pulsar!" -ForegroundColor Yellow

$confirmation = Read-Host "`nAre you sure? (yes/no)"

if ($confirmation -eq "yes") {
    Write-Host "`n🗑️  Deleting namespace and all resources..." -ForegroundColor Red
    kubectl delete namespace url-shortener

    Write-Host "`n✅ All resources deleted!" -ForegroundColor Green
} else {
    Write-Host "`n❌ Operation cancelled." -ForegroundColor Yellow
}
