# 📊 Check Kubernetes Infrastructure Status

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Kubernetes Status Check" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

Write-Host "`n📦 Namespace:" -ForegroundColor Yellow
kubectl get namespace url-shortener

Write-Host "`n📊 All Resources:" -ForegroundColor Yellow
kubectl get all -n url-shortener

Write-Host "`n🔧 ConfigMaps:" -ForegroundColor Yellow
kubectl get configmap -n url-shortener

Write-Host "`n💾 Persistent Volume Claims:" -ForegroundColor Yellow
kubectl get pvc -n url-shortener

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Pod Details:" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# PostgreSQL
Write-Host "`n🐘 PostgreSQL:" -ForegroundColor Green
kubectl get pod -n url-shortener -l app=postgres -o wide
$pgPod = kubectl get pod -n url-shortener -l app=postgres -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($pgPod) {
    Write-Host "   Status: " -NoNewline -ForegroundColor Gray
    kubectl get pod -n url-shortener $pgPod -o jsonpath='{.status.phase}'
    Write-Host ""
}

# Valkey
Write-Host "`n🔴 Valkey:" -ForegroundColor Green
kubectl get pod -n url-shortener -l app=valkey -o wide
$valkeyPod = kubectl get pod -n url-shortener -l app=valkey -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($valkeyPod) {
    Write-Host "   Status: " -NoNewline -ForegroundColor Gray
    kubectl get pod -n url-shortener $valkeyPod -o jsonpath='{.status.phase}'
    Write-Host ""
}

# Pulsar
Write-Host "`n📨 Pulsar:" -ForegroundColor Green
kubectl get pod -n url-shortener -l app=pulsar -o wide
$pulsarPod = kubectl get pod -n url-shortener -l app=pulsar -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($pulsarPod) {
    Write-Host "   Status: " -NoNewline -ForegroundColor Gray
    kubectl get pod -n url-shortener $pulsarPod -o jsonpath='{.status.phase}'
    Write-Host ""
}

# Keycloak
Write-Host "`n🔐 Keycloak:" -ForegroundColor Green
kubectl get pod -n url-shortener -l app=keycloak -o wide
$keycloakPod = kubectl get pod -n url-shortener -l app=keycloak -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($keycloakPod) {
    Write-Host "   Status: " -NoNewline -ForegroundColor Gray
    kubectl get pod -n url-shortener $keycloakPod -o jsonpath='{.status.phase}'
    Write-Host ""
}

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "Service Endpoints:" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
kubectl get services -n url-shortener

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Access URLs (NodePort):" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host "   PostgreSQL:   localhost:30432" -ForegroundColor White
Write-Host "   Valkey:       localhost:30379" -ForegroundColor White
Write-Host "   Pulsar:       localhost:30650" -ForegroundColor White
Write-Host "   Pulsar Admin: localhost:30081" -ForegroundColor White
Write-Host "   Keycloak:     localhost:30180" -ForegroundColor White

Write-Host "`n💡 Useful Commands:" -ForegroundColor Cyan
Write-Host "   Watch pods:        kubectl get pods -n url-shortener -w" -ForegroundColor Gray
Write-Host "   Describe pod:      kubectl describe pod <pod-name> -n url-shortener" -ForegroundColor Gray
Write-Host "   View logs:         kubectl logs <pod-name> -n url-shortener" -ForegroundColor Gray
Write-Host "   Shell into pod:    kubectl exec -it <pod-name> -n url-shortener -- /bin/sh" -ForegroundColor Gray
Write-Host "   Delete all:        kubectl delete namespace url-shortener" -ForegroundColor Gray
