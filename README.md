# Bakti Marsada

Monorepo untuk:

- `app`: Android app berbasis Compose, MVVM, StateFlow, Hilt, Room, Firebase, dan enkripsi local storage
- `cloudflare-be`: backend Cloudflare Workers + Hono + D1

## Struktur

```text
AndroidStudioProjects/BaktiMarsada
├── app
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
