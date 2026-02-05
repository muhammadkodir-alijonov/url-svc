# 🚀 Deploy Infrastructure to Kubernetes

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Kubernetes Infrastructure Setup" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

Write-Host "`n📦 Checking Kubernetes..." -ForegroundColor Yellow
kubectl version --short

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Step 1: Create Namespace" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
kubectl apply -f infrastructure/kubernetes/namespace.yaml

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Step 2: Deploy PostgreSQL" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
kubectl apply -f infrastructure/kubernetes/postgres-statefulset.yaml

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Step 3: Deploy Valkey/Redis" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
kubectl apply -f infrastructure/kubernetes/valkey-statefulset.yaml

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Step 4: Deploy Pulsar" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
kubectl apply -f infrastructure/kubernetes/pulsar-statefulset.yaml

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Step 5: Deploy Keycloak" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
kubectl apply -f infrastructure/kubernetes/keycloak-deployment.yaml

Write-Host "`n================================" -ForegroundColor Yellow
Write-Host "Waiting for pods to be ready..." -ForegroundColor Yellow
Write-Host "================================" -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host "`n📊 Checking deployment status..." -ForegroundColor Cyan
kubectl get pods -n url-shortener
kubectl get services -n url-shortener

Write-Host "`n================================" -ForegroundColor Green
Write-Host "Infrastructure Deployed!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

Write-Host "`n📱 Services are exposed via NodePort:" -ForegroundColor Cyan
Write-Host "   PostgreSQL:  localhost:30432" -ForegroundColor White
Write-Host "   Valkey:      localhost:30379" -ForegroundColor White
Write-Host "   Pulsar:      localhost:30650" -ForegroundColor White
Write-Host "   Pulsar Admin:localhost:30081" -ForegroundColor White
Write-Host "   Keycloak:    localhost:30180" -ForegroundColor White

Write-Host "`n💡 Update application-dev.yml:" -ForegroundColor Yellow
Write-Host "   PostgreSQL: localhost:30432" -ForegroundColor Gray
Write-Host "   Redis:      localhost:30379" -ForegroundColor Gray
Write-Host "   Pulsar:     localhost:30650" -ForegroundColor Gray
Write-Host "   Keycloak:   localhost:30180" -ForegroundColor Gray

Write-Host "`n✅ Next steps:" -ForegroundColor Cyan
Write-Host "   1. Wait for all pods to be Running (check with: kubectl get pods -n url-shortener -w)" -ForegroundColor White
Write-Host "   2. Run: .\check-k8s-status.ps1" -ForegroundColor White
Write-Host "   3. Run: .\start-dev.ps1" -ForegroundColor White
