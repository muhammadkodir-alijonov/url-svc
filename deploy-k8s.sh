#!/bin/bash
# Deploy Infrastructure to Kubernetes

echo "================================"
echo "Kubernetes Infrastructure Setup"
echo "================================"

echo ""
echo "Checking Kubernetes..."
kubectl version --short

echo ""
echo "================================"
echo "Step 1: Create Namespace"
echo "================================"
kubectl apply -f infrastructure/kubernetes/namespace.yaml

echo ""
echo "================================"
echo "Step 2: Deploy PostgreSQL"
echo "================================"
kubectl apply -f infrastructure/kubernetes/postgres-statefulset.yaml

echo ""
echo "================================"
echo "Step 3: Deploy Valkey/Redis"
echo "================================"
kubectl apply -f infrastructure/kubernetes/valkey-statefulset.yaml

echo ""
echo "================================"
echo "Step 4: Deploy Pulsar"
echo "================================"
kubectl apply -f infrastructure/kubernetes/pulsar-statefulset.yaml

echo ""
echo "================================"
echo "Step 5: Deploy Keycloak"
echo "================================"
kubectl apply -f infrastructure/kubernetes/keycloak-deployment.yaml

echo ""
echo "================================"
echo "Step 6: Deploy Vault"
echo "================================"
kubectl apply -f infrastructure/kubernetes/vault-statefulset.yaml

echo ""
echo "================================"
echo "Step 7: Deploy APISIX Gateway"
echo "================================"
kubectl apply -f infrastructure/kubernetes/apisix-deployment.yaml

echo ""
echo "================================"
echo "Waiting for pods to be ready..."
echo "================================"
sleep 10

echo ""
echo "Checking deployment status..."
kubectl get pods -n url-shorten
kubectl get services -n url-shorten

echo ""
echo "================================"
echo "Infrastructure Deployed!"
echo "================================"

echo ""
echo "Services are exposed via NodePort:"
echo "   PostgreSQL:       localhost:30432"
echo "   Valkey:           localhost:30379"
echo "   Pulsar:           localhost:30650"
echo "   Pulsar Admin:     localhost:30081"
echo "   Keycloak:         localhost:30180"
echo "   Vault:            localhost:30200"
echo "   APISIX Gateway:   localhost:30900"
echo "   APISIX Admin:     localhost:30901"
echo "   APISIX Dashboard: localhost:30910"

echo ""
echo "Update application-dev.yml:"
echo "   PostgreSQL: localhost:30432"
echo "   Redis:      localhost:30379"
echo "   Pulsar:     localhost:30650"
echo "   Keycloak:   localhost:30180"

echo ""
echo "Next steps:"
echo "   1. Wait for all pods to be Running (check with: kubectl get pods -n url-shorten -w)"
echo "   2. Run: ./check-k8s-status.sh"
echo "   3. Setup Vault: export VAULT_ADDR='http://localhost:30200' && export VAULT_TOKEN='dev-root-token'"
echo "   4. Access APISIX Dashboard: http://localhost:30910 (admin/admin)"
echo "   5. Run your application: ./mvnw quarkus:dev -Dquarkus.profile=dev"

echo ""
echo "================================"
read -p "Press ENTER to exit..." dummy

