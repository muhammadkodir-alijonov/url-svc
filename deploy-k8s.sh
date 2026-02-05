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
echo "Waiting for pods to be ready..."
echo "================================"
sleep 5

echo ""
echo "Checking deployment status..."
kubectl get pods -n url-shortener
kubectl get services -n url-shortener

echo ""
echo "================================"
echo "Infrastructure Deployed!"
echo "================================"

echo ""
echo "Services are exposed via NodePort:"
echo "   PostgreSQL:   localhost:30432"
echo "   Valkey:       localhost:30379"
echo "   Pulsar:       localhost:30650"
echo "   Pulsar Admin: localhost:30081"
echo "   Keycloak:     localhost:30180"

echo ""
echo "Update application-dev.yml:"
echo "   PostgreSQL: localhost:30432"
echo "   Redis:      localhost:30379"
echo "   Pulsar:     localhost:30650"
echo "   Keycloak:   localhost:30180"

echo ""
echo "Next steps:"
echo "   1. Wait for all pods to be Running (check with: kubectl get pods -n url-shortener -w)"
echo "   2. Run: ./check-k8s-status.sh"
echo "   3. Run your application"
