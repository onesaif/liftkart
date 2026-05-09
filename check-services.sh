#!/bin/bash
echo "Checking services..."
curl -s -o /dev/null -w "Auth (8081):         %{http_code}\n" http://localhost:8081/v3/api-docs
curl -s -o /dev/null -w "Product (8082):      %{http_code}\n" http://localhost:8082/v3/api-docs
curl -s -o /dev/null -w "Cart (8083):         %{http_code}\n" http://localhost:8083/v3/api-docs
curl -s -o /dev/null -w "Order (8084):        %{http_code}\n" http://localhost:8084/v3/api-docs
curl -s -o /dev/null -w "Notification (8085): %{http_code}\n" http://localhost:8085/v3/api-docs
curl -s -o /dev/null -w "Analytics (8086):    %{http_code}\n" http://localhost:8086/v3/api-docs
