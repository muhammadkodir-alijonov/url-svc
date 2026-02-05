#!/bin/bash
# Check Kubernetes Infrastructure Status

echo "================================"
echo "Kubernetes Status Check"
echo "================================"

echo ""
echo "Namespace:"
kubectl get namespace url-shortener

echo ""
echo "All Resources:"
kubectl get all -n url-shortener

echo ""
echo "ConfigMaps:"
kubectl get configmap -n url-shortener

echo ""
echo "Persistent Volume Claims:"
kubectl get pvc -n url-shortener

echo ""
echo "================================"
echo "Pod Details:"
echo "================================"

# PostgreSQL
echo ""
echo "PostgreSQL:"
kubectl get pod -n url-shortener -l app=postgres -o wide
pgPod=$(kubectl get pod -n url-shortener -l app=postgres -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$pgPod" ]; then
    echo -n "   Status: "
    kubectl get pod -n url-shortener $pgPod -o jsonpath='{.status.phase}'
    echo ""
fi

# Valkey
echo ""
echo "Valkey:"
kubectl get pod -n url-shortener -l app=valkey -o wide
valkeyPod=$(kubectl get pod -n url-shortener -l app=valkey -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$valkeyPod" ]; then
    echo -n "   Status: "
    kubectl get pod -n url-shortener $valkeyPod -o jsonpath='{.status.phase}'
    echo ""
fi

# Pulsar
echo ""
echo "Pulsar:"
kubectl get pod -n url-shortener -l app=pulsar -o wide
pulsarPod=$(kubectl get pod -n url-shortener -l app=pulsar -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$pulsarPod" ]; then
    echo -n "   Status: "
    kubectl get pod -n url-shortener $pulsarPod -o jsonpath='{.status.phase}'
    echo ""
fi

# Keycloak
echo ""
echo "Keycloak:"
kubectl get pod -n url-shortener -l app=keycloak -o wide
keycloakPod=$(kubectl get pod -n url-shortener -l app=keycloak -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$keycloakPod" ]; then
    echo -n "   Status: "
    kubectl get pod -n url-shortener $keycloakPod -o jsonpath='{.status.phase}'
    echo ""
fi

echo ""
echo "================================"
echo "Service Endpoints:"
echo "================================"
kubectl get services -n url-shortener

echo ""
echo "================================"
echo "Access URLs (NodePort):"
echo "================================"
echo "   PostgreSQL:       localhost:30432"
echo "   Valkey:           localhost:30379"
echo "   Pulsar:           localhost:30650"
echo "   Pulsar Admin:     localhost:30081"
echo "   Keycloak:         localhost:30180"
echo "   Vault:            http://localhost:30200"
echo "   APISIX Gateway:   http://localhost:30900"
echo "   APISIX Admin API: http://localhost:30901"
echo "   APISIX Dashboard: http://localhost:30910"

echo ""
echo "Useful Commands:"
echo "   Watch pods:        kubectl get pods -n url-shortener -w"
echo "   Describe pod:      kubectl describe pod POD_NAME -n url-shortener"
echo "   View logs:         kubectl logs POD_NAME -n url-shortener"
echo "   Shell into pod:    kubectl exec -it POD_NAME -n url-shortener -- /bin/sh"
echo "   Delete all:        kubectl delete namespace url-shortener"
