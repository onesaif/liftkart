#!/bin/bash
echo "🚀 Starting LiftKart..."

# Force Java 22
export JAVA_HOME=$(/usr/libexec/java_home -v 22)
export PATH="$JAVA_HOME/bin:$PATH"
echo "✅ Java: $(java -version 2>&1 | head -1)"

BASE="$(cd "$(dirname "$0")" && pwd)"

# Start infrastructure
echo "🐳 Starting Docker containers..."
docker-compose -f "$BASE/docker-compose.yml" up -d postgres rabbitmq zookeeper kafka
echo "⏳ Waiting 15s for infrastructure..."
sleep 15

# Start backend services
echo "🔧 Starting backend services..."
for svc in auth-service product-service cart-service order-service notification-service analytics-service api-gateway; do
  cd "$BASE/services/$svc"
  nohup mvn spring-boot:run > app.log 2>&1 &
  echo "  ▶ $svc started (PID: $!)"
  cd "$BASE"
done

# Start frontend
echo "⚛️  Starting React frontend..."
cd "$BASE/frontend"
nohup npm start > app.log 2>&1 &
echo "  ▶ frontend started (PID: $!)"

echo ""
echo "⏳ Services starting up — wait 90 seconds then open:"
echo "   http://localhost:3000"
echo ""
echo "To check services: bash check-services.sh"
