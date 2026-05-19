#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_NAME="${DB_NAME:-bakti_marsada_db}"
SKIP_TESTS="${SKIP_TESTS:-false}"
FORCE_MIGRATE="${FORCE_MIGRATE:-false}"

echo "[deploy-prod] root=${ROOT_DIR}"
echo "[deploy-prod] db=${DB_NAME}"
echo "[deploy-prod] force_migrate=${FORCE_MIGRATE}"

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

needs_migration() {
  local local_files remote_names missing_count

  local_files="$(find migrations -maxdepth 1 -type f -name '*.sql' -exec basename {} \; | sort || true)"
  if [[ -z "${local_files}" ]]; then
    echo "[deploy-prod] no local migration files found"
    return 1
  fi

  remote_names="$(
    npx wrangler d1 execute "${DB_NAME}" \
      --remote \
      --command "SELECT name FROM d1_migrations ORDER BY id;" \
      2>/dev/null \
      | grep -E '^[[:space:]]*\│' \
      | sed 's/^[[:space:]]*│//; s/│.*$//' \
      | tr -d '[:space:]' \
      || true
  )"

  if [[ -z "${remote_names}" ]]; then
    echo "[deploy-prod] unable to read remote migration history, will run migration apply for safety"
    return 0
  fi

  missing_count="$(
    while IFS= read -r migration_name; do
      [[ -z "${migration_name}" ]] && continue
      if ! grep -qx "${migration_name}" <<<"${remote_names}"; then
        echo "${migration_name}"
      fi
    done <<<"${local_files}" | wc -l | tr -d ' '
  )"

  if [[ "${missing_count}" -gt 0 ]]; then
    echo "[deploy-prod] detected ${missing_count} pending migration(s)"
    return 0
  fi

  echo "[deploy-prod] no pending migrations detected"
  return 1
}

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

if [[ "${FORCE_MIGRATE}" == "true" ]]; then
  echo "[deploy-prod] FORCE_MIGRATE=true -> applying remote migrations"
  npx wrangler d1 migrations apply "${DB_NAME}" --remote
elif needs_migration; then
  echo "[deploy-prod] applying remote migrations"
  npx wrangler d1 migrations apply "${DB_NAME}" --remote
else
  echo "[deploy-prod] migration step skipped"
fi

echo "[deploy-prod] deploying worker"
npm run deploy

echo "[deploy-prod] done"
