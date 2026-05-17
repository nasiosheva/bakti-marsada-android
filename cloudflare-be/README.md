# Bakti Marsada BE

Cloudflare backend using Workers + Hono + D1.

## Setup

1. Create the D1 database and copy the database id into `wrangler.toml`.
2. Install dependencies.
3. Apply migrations.
4. Run local dev.

## Commands

```bash
npm install
npm run check
npm run test
npm run dev
npm run deploy
```

## D1

Create a new database:

```bash
npx wrangler d1 create bakti_marsada_db
```

Apply migrations locally:

```bash
npx wrangler d1 migrations apply bakti_marsada_db --local
```

Apply migrations remotely:

```bash
npx wrangler d1 migrations apply bakti_marsada_db
```
