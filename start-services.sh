#!/bin/bash
BASE="$(cd "$(dirname "$0")" && pwd)/services"

# Force Java 22 to avoid Lombok incompatibility with Java 25
export JAVA_HOME=$(/usr/libexec/java_home -v 22)
export PATH="$JAVA_HOME/bin:$PATH"
echo "Using Java: $(java -version 2>&1 | head -1)"

echo "Killing existing services..."
pkill -f "spring-boot:run" 2>/dev/null
sleep 3

start_service() {
  local svc=$1
  local port=$2
  echo "Starting $svc on port $port..."
  cd "$BASE/$svc"
  nohup mvn clean spring-boot:run > app.log 2>&1 &
  echo "$svc PID: $!"
  cd "$BASE"
}

start_service "auth-service"         8081
start_service "product-service"      8082
start_service "cart-service"         8083
start_service "order-service"        8084
start_service "notification-service" 8085
start_service "analytics-service"    8086

echo ""
echo "All services starting. Wait 90 seconds then run: bash check-services.sh"
