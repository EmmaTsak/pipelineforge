
#!/usr/bin/env bash
set -Eeuo pipefail

IMAGE="${1:?Usage: smoke-test-image.sh IMAGE_TAG}"
CONTAINER="pipelineforge-smoke-${BUILD_NUMBER:-local}-$$"

cleanup() {
    result=$?

    if [ "$result" -ne 0 ]; then
        echo "Smoke test failed. Container logs:"
        docker logs "$CONTAINER" 2>&1 || true
    fi

    docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
}

trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

echo "Checking image: $IMAGE"

command -v curl >/dev/null
docker image inspect "$IMAGE" >/dev/null

echo "Starting temporary container..."

docker run \
    --pull=never \
    --detach \
    --name "$CONTAINER" \
    --publish 127.0.0.1::8080 \
    "$IMAGE" >/dev/null

PORT=$(docker port "$CONTAINER" 8080/tcp |
    awk -F: 'END {print $NF}')

if [[ ! "$PORT" =~ ^[0-9]+$ ]]; then
    echo "Could not determine published port."
    exit 1
fi

BASE_URL="http://127.0.0.1:$PORT"
HEALTHY=false

echo "Waiting for Spring Boot at $BASE_URL ..."

for attempt in {1..45}; do

    if curl -fsS --max-time 3 \
        "$BASE_URL/actuator/health" 2>/dev/null |
        grep -q '"status":"UP"'; then

        HEALTHY=true
        break
    fi

    if [ "$(docker inspect --format '{{.State.Running}}' "$CONTAINER")" != "true" ]; then
        echo "Container exited unexpectedly."
        exit 1
    fi

    sleep 2
done

if [ "$HEALTHY" != true ]; then
    echo "Health check timed out."
    exit 1
fi

echo "Health endpoint: PASS"

SITES=$(curl -fsS --max-time 5 "$BASE_URL/api/sites")

grep -Fq '"id":"cork-01"' <<< "$SITES"
grep -Fq '"id":"dublin-01"' <<< "$SITES"

echo "Sites endpoint: PASS"
echo "SMOKE TEST PASSED"
