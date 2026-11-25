#!/usr/bin/env bash
set -euo pipefail

cd /home/ubuntu/BuscaPeli
test -f .env
git fetch --quiet origin main
git merge --ff-only origin/main

revision="$(git rev-parse HEAD)"
if test -f .deployed-revision && test "$(<.deployed-revision)" = "$revision" && test "$(docker compose ps --status running --services | wc -l)" -eq 3; then
    exit 0
fi

docker compose up -d --build
for attempt in $(seq 1 90); do
    if curl -fsS --max-time 5 http://127.0.0.1:8088/api/v1/health | python3 -c 'import json,sys; d=json.load(sys.stdin); assert d["movies"] == 1500 and d["ratings"] > 0' 2>/dev/null; then
        printf '%s\n' "$revision" > .deployed-revision
        docker builder prune -f --filter 'until=168h' >/dev/null
        exit 0
    fi
    sleep 5
done
docker compose logs --tail 80 backend frontend
exit 1
