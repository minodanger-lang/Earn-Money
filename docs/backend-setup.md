# Backend and Supabase setup

## Current API routes

- `GET /health`: service status and whether Supabase environment settings exist.
- `POST /api/auth/register`: email/password account registration.
- `POST /api/auth/login`: email/password sign-in.
- `GET /api/me`: authenticated account/profile.
- `GET /api/tasks`: active tasks from the database.
- `GET /api/wallet`: pending/available ledger totals and recent entries.
- `POST /api/withdrawals`: intentionally returns 503 until payout approval and testing.\n- `GET /api/admin/users`: protected user listing.\n- `GET /api/admin/withdrawals`: protected queue of requested/reviewing withdrawals.\n- `POST /api/admin/withdrawals/:id/review`: moves a request from requested to reviewing; it cannot mark a request as paid.\n- `POST /api/admin/tasks`: creates an inactive task record; publishing remains a separate controlled step.

All account-specific routes require `Authorization: Bearer <access_token>`.

## Required environment

Copy `backend/.env.example` to a private environment configuration and set:

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`
- `PORT` (optional)
- `ALLOWED_ORIGINS` (optional comma-separated exact origins)\n- `ADMIN_USER_IDS` (comma-separated Supabase Auth user UUIDs permitted to call admin routes)

Never put the service-role key in the Android app, public Git, or a client-side build.

## Database setup

Apply `database/migrations/001_initial_schema.sql` to the intended Supabase project. The migration creates profiles, provider task catalogue, an append-only wallet ledger, a balance view, and withdrawal request storage. It does not create fake tasks, credit money, or enable withdrawals.

Configure Supabase Auth email confirmation and production password/security policies before public launch. Add only trusted administrator UUIDs to `ADMIN_USER_IDS`; never expose the service-role key.

## Important launch gates

The API currently has no approved offer provider or payout integration. Task catalogue rows must only be populated from a provider contract that permits cash incentives in Egypt. Do not manually insert tasks that imply guaranteed payment.

Wallet writes must remain server-side. Provider callbacks need the exact signature-validation algorithm documented by the selected provider, idempotency handling, reversal handling, and reconciliation before any real reward is credited. Withdrawal creation and payment execution remain disabled until a real payout method has been approved and tested.

## Deploying the API on Vercel

The API includes a Vercel Node.js catch-all function at `backend/api/[...path].js`. When creating or configuring the Vercel project for this API, set **Root Directory** to `backend` and leave the root directory setting enabled for this project. The deployed health endpoint is `/api/health`; the standalone local server also supports `/health`. Add the Supabase environment variables in Vercel Project Settings → Environment Variables for the environments you intend to test. Never paste the service-role key into GitHub or the Android app.
