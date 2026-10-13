#!/usr/bin/env bash
# Smoke test da API. Uso: ./scripts/smoke-test.sh <BASE_URL> [AMBIENTE_ESPERADO]
# Se SMOKE_USER e SMOKE_PASSWORD estiverem definidos, tambem testa um endpoint autenticado.
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
EXPECTED_ENV="${2:-}"
TIMEOUT="${SMOKE_TIMEOUT:-600}"

echo ">> Aguardando ${BASE_URL}/actuator/health (timeout ${TIMEOUT}s)"
elapsed=0
until curl -fsS "${BASE_URL}/actuator/health" | grep -q '"status":"UP"'; do
  if [ "$elapsed" -ge "$TIMEOUT" ]; then
    echo "ERRO: aplicacao nao ficou saudavel em ${TIMEOUT}s"
    exit 1
  fi
  sleep 10
  elapsed=$((elapsed + 10))
done
echo "OK: health UP"

info="$(curl -fsS "${BASE_URL}/actuator/info")"
echo "Info: ${info}"
if [ -n "$EXPECTED_ENV" ] && ! echo "$info" | grep -q "\"environment\":\"${EXPECTED_ENV}\""; then
  echo "ERRO: ambiente esperado '${EXPECTED_ENV}' nao encontrado em /actuator/info"
  exit 1
fi

code="$(curl -s -o /dev/null -w '%{http_code}' "${BASE_URL}/api/licenses")"
[ "$code" = "401" ] || { echo "ERRO: esperado 401 sem credenciais, recebido ${code}"; exit 1; }
echo "OK: endpoint protegido retorna 401 sem credenciais"

code="$(curl -s -o /dev/null -w '%{http_code}' "${BASE_URL}/swagger-ui.html")"
case "$code" in 200|302) echo "OK: Swagger UI disponivel (${code})" ;; *) echo "ERRO: Swagger retornou ${code}"; exit 1 ;; esac

if [ -n "${SMOKE_USER:-}" ] && [ -n "${SMOKE_PASSWORD:-}" ]; then
  curl -fsS -u "${SMOKE_USER}:${SMOKE_PASSWORD}" "${BASE_URL}/api/dashboard/summary"
  echo
  echo "OK: dashboard autenticado respondeu"
fi

echo ">> Smoke test concluido com sucesso"
