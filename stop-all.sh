#!/bin/bash
echo "🛑 Stopping LiftKart..."
BASE="$(cd "$(dirname "$0")" && pwd)"
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "react-scripts" 2>/dev/null
cd "$BASE"
docker-compose down
echo "✅ All services stopped!"
