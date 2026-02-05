#!/bin/bash
echo "   Delete all:        kubectl delete namespace url-shortener"
echo "   Shell into pod:    kubectl exec -it POD_NAME -n url-shortener -- /bin/sh"
echo "   View logs:         kubectl logs POD_NAME -n url-shortener"
echo "   Describe pod:      kubectl describe pod POD_NAME -n url-shortener"
echo "   Watch pods:        kubectl get pods -n url-shortener -w"
echo "Useful Commands:"
echo ""

echo "   Keycloak:     localhost:30180"
echo "   Pulsar Admin: localhost:30081"
echo "   Pulsar:       localhost:30650"
echo "   Valkey:       localhost:30379"
echo "   PostgreSQL:   localhost:30432"
echo "================================"
echo "Access URLs (NodePort):"
echo "================================"
echo ""

kubectl get services -n url-shortener
echo "================================"
echo "Service Endpoints:"
echo "================================"
echo ""

fi
    echo ""
    kubectl get pod -n url-shortener $keycloakPod -o jsonpath='{.status.phase}'
    echo -n "   Status: "
if [ -n "$keycloakPod" ]; then
keycloakPod=$(kubectl get pod -n url-shortener -l app=keycloak -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
kubectl get pod -n url-shortener -l app=keycloak -o wide
echo "Keycloak:"
echo ""
# Keycloak

fi
    echo ""
    kubectl get pod -n url-shortener $pulsarPod -o jsonpath='{.status.phase}'
    echo -n "   Status: "
if [ -n "$pulsarPod" ]; then
pulsarPod=$(kubectl get pod -n url-shortener -l app=pulsar -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
kubectl get pod -n url-shortener -l app=pulsar -o wide
echo "Pulsar:"
echo ""
# Pulsar

fi
    echo ""
    kubectl get pod -n url-shortener $valkeyPod -o jsonpath='{.status.phase}'
    echo -n "   Status: "
if [ -n "$valkeyPod" ]; then
valkeyPod=$(kubectl get pod -n url-shortener -l app=valkey -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
kubectl get pod -n url-shortener -l app=valkey -o wide
echo "Valkey:"
echo ""
# Valkey

fi
    echo ""
    kubectl get pod -n url-shortener $pgPod -o jsonpath='{.status.phase}'
    echo -n "   Status: "
if [ -n "$pgPod" ]; then
pgPod=$(kubectl get pod -n url-shortener -l app=postgres -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
kubectl get pod -n url-shortener -l app=postgres -o wide
echo "PostgreSQL:"
echo ""
# PostgreSQL

echo "================================"
echo "Pod Details:"
echo "================================"
echo ""

kubectl get pvc -n url-shortener
echo "Persistent Volume Claims:"
echo ""

kubectl get configmap -n url-shortener
echo "ConfigMaps:"
echo ""

kubectl get all -n url-shortener
echo "All Resources:"
echo ""

kubectl get namespace url-shortener
echo "Namespace:"
echo ""

echo "================================"
echo "Kubernetes Status Check"
echo "================================"

# Check Kubernetes Infrastructure Status
