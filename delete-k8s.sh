#!/bin/bash
# Delete All Kubernetes Resources

echo "================================"
echo "Delete Kubernetes Infrastructure"
echo "================================"

echo ""
echo "WARNING: This will delete all resources in url-shorten namespace!"
echo "   Including all data in PostgreSQL, Valkey, and Pulsar!"

read -p "Are you sure? (yes/no): " confirmation

if [ "$confirmation" = "yes" ]; then
    echo ""
    echo "Deleting namespace and all resources..."
    kubectl delete namespace url-shorten

    echo ""
    echo "All resources deleted!"
else
    echo ""
    echo "Operation cancelled."
fi

echo ""
read -p "Press ENTER to exit..." dummy

