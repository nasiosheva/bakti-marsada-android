# Bakti Marsada

Monorepo untuk:

- `app`: Android app berbasis Compose, MVVM, StateFlow, Hilt, Room, Firebase, dan enkripsi local storage
- `cloudflare-be`: backend Cloudflare Workers + Hono + D1

## Struktur

```text
AndroidStudioProjects/BaktiMarsada
├── app
├── core
├── domain
├── data
├── data-source-cloudflare
├── data-source-python
├── data-source-firebase
├── data-source-simulate
└── cloudflare-be
```

## Android App

Lokasi:

- [`app/src/main/java/com/lampung/baktimarsada`](./app/src/main/java/com/lampung/baktimarsada)

Fitur baseline:

- MVVM + StateFlow
- event/state terpisah
- reusable UI components
- Room
- Firebase Messaging
- Cloud Firestore telemetry
- encrypted storage

Jalankan test:

```bash
./gradlew test
```

Flow real backend (non-simulate):

- Build type `debug` sekarang default ke `http://10.0.2.2:8787/` untuk emulator Android.
- Jalankan backend lokal dulu:

```bash
cd cloudflare-be
npm run dev
```

- Lalu jalankan app debug dan login dengan akun yang sudah terdaftar di endpoint `/auth/login`.

Switch sumber data app (`cloudflare` default):

```bash
./gradlew :app:assembleDebug -PdataSourceProvider=cloudflare
./gradlew :app:assembleDebug -PdataSourceProvider=python -PpythonApiBaseUrl=http://10.0.2.2:8000/
./gradlew :app:assembleDebug -PdataSourceProvider=firebase
```

Opsional override base URL:

- `-PcloudflareApiBaseUrl=...`
- `-PpythonApiBaseUrl=...`

### Google Sign-In Debug

Google Sign-In bisa diaktifkan tanpa `google-services.json` khusus debug dengan mengirim Web Client ID dari command line:

```bash
./gradlew :app:assembleDebug -PdebugGoogleWebClientId=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com
```

Alternatif global untuk semua build type:

```bash
./gradlew :app:assembleDebug -PgoogleWebClientId=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com
```

Urutan konfigurasi:

- `debugGoogleWebClientId`, `simulateGoogleWebClientId`, atau `releaseGoogleWebClientId`
- fallback ke `googleWebClientId`
- fallback ke environment variable `DEBUG_GOOGLE_WEB_CLIENT_ID`, `SIMULATE_GOOGLE_WEB_CLIENT_ID`, `RELEASE_GOOGLE_WEB_CLIENT_ID`, atau `GOOGLE_WEB_CLIENT_ID`

Pastikan OAuth Client di Firebase/Google Cloud sudah memakai package build yang sesuai, misalnya `com.lampung.baktimarsada.dev` untuk debug.

### Multi-tenant clone

Tenant aktif bisa diganti saat build:

```bash
./gradlew :app:assembleDebug -PtenantKey=hkbp-kedaton
./gradlew :app:assembleDebug -PtenantKey=hkbp-bandarjaya
```

Tenant profile terpusat di:

- [`core/src/main/java/com/lampung/baktimarsada/core/tenant/TenantRegistry.kt`](./core/src/main/java/com/lampung/baktimarsada/core/tenant/TenantRegistry.kt)

Struktur profile sudah mendukung:

- `tenant` utama (contoh: `HKBP`)
- `sub-tenant` / gereja lokal (contoh: `HKBP Kedaton`)
- sektor/wijk tetap per user/session

Untuk clone tenant baru, cukup tambah 1 profile baru di registry tanpa ubah flow domain/repository/datasource.

## Cloudflare Backend

Lokasi:

- [`cloudflare-be`](./cloudflare-be)

Stack:

- TypeScript
- Hono
- Cloudflare Workers
- D1

Command utama:

```bash
cd cloudflare-be
npm install
npm run check
npm run test
npx wrangler dev
```

Deploy preview / remote:

```bash
npx wrangler deploy
```

## Cloudflare Setup

Database sudah disiapkan dengan:

- `bakti_marsada_db`

Binding ada di:

- [`cloudflare-be/wrangler.toml`](./cloudflare-be/wrangler.toml)

Migrasi awal:

- `users`
- `sessions`
- `audit_logs`

## Auth Endpoint

Endpoint baseline:

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`
- `POST /auth/logout`

### Contoh Request

Register:

```bash
curl -X POST http://localhost:8787/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

Login:

```bash
curl -X POST http://localhost:8787/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Me:

```bash
curl http://localhost:8787/auth/me \
  -H "Authorization: Bearer SESSION_TOKEN"
```

Logout:

```bash
curl -X POST http://localhost:8787/auth/logout \
  -H "Authorization: Bearer SESSION_TOKEN"
```

### Contoh Response

Register / login:

```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "Test User",
    "role": "user",
    "isActive": true,
    "createdAt": "2026-05-17T00:00:00.000Z",
    "updatedAt": "2026-05-17T00:00:00.000Z"
  },
  "session": {
    "sessionId": "uuid",
    "sessionToken": "token",
    "expiresAt": "2026-05-24T00:00:00.000Z"
  }
}
```

Health:

```json
{
  "ok": true,
  "database": "connected"
}
```

## Notes

- `cloudflare-be` menggunakan `development` sebagai branch lokal default yang saya pakai saat kerja.
- Remote GitHub perlu write access untuk bisa push ke repo target.
