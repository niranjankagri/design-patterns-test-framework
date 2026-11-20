#!/usr/bin/env bash
# Starts the Pattern Shop image on the host network (port 8080) and waits until it answers /health.
# The host network lets a Selenium Grid container (also on the host network) reach the shop at localhost.
# Usage: scripts/start-shop.sh [image]   default ghcr.io/niranjankagri/pattern-shop-demo-app:latest
set -euo pipefail
image="${1:-ghcr.io/niranjankagri/pattern-shop-demo-app:latest}"
docker run -d --name shop --network host "$image" >/dev/null
for i in $(seq 1 30); do
  if curl -fsS -m 2 http://localhost:8080/health >/dev/null 2>&1; then
    echo "Shop ready: $image ($(docker image inspect "$image" --format '{{index .RepoDigests 0}}'))"
    exit 0
  fi
  sleep 1
done
docker logs shop
echo "The shop did not answer /health within 30 s" >&2
exit 1
