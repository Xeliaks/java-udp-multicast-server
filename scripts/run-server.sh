#!/usr/bin/env bash
# Run the multicast game event server (for EC2 or any Linux host).
# Usage: ./run-server.sh [multicast-group] [port]
# Example: ./run-server.sh 230.0.0.1 6789

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR="$PROJECT_DIR/target/udp-multicast-game-server-1.0.0-SNAPSHOT.jar"

if [[ ! -f "$JAR" ]]; then
  echo "JAR not found. Build first: mvn -q package"
  exit 1
fi

GROUP="${1:-230.0.0.1}"
PORT="${2:-6789}"

echo "Starting server on $GROUP:$PORT (headless). Stop with Ctrl+C."
exec java -Dserver.headless=true -jar "$JAR" "$GROUP" "$PORT"
