#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_NAME="${DB_NAME:-bakti_marsada_db}"
SKIP_TESTS="${SKIP_TESTS:-false}"

echo "[deploy-prod] root=${ROOT_DIR}"
echo "[deploy-prod] db=${DB_NAME}"

cd "${ROOT_DIR}"

if ! command -v npm >/dev/null 2>&1; then
  echo "[deploy-prod] npm not found"
  exit 1
fi

if ! command -v npx >/dev/null 2>&1; then
  echo "[deploy-prod] npx not found"
  exit 1
fi

if [[ ! -f "wrangler.toml" ]]; then
  echo "[deploy-prod] wrangler.toml not found in ${ROOT_DIR}"
  exit 1
fi

echo "[deploy-prod] installing dependencies"
npm install

echo "[deploy-prod] running type check"
npm run check

if [[ "${SKIP_TESTS}" != "true" ]]; then
  echo "[deploy-prod] running tests"
  npm run test
else
  echo "[deploy-prod] SKIP_TESTS=true -> tests skipped"
fi

echo "[deploy-prod] applying remote migrations"
npx wrangler d1 migrations apply "${DB_NAME}" --remote

echo "[deploy-prod] deploying worker"
npm run deploy

echo "[deploy-prod] done"
