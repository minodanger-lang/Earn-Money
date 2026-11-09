# Backend and Supabase setup

## Current API routes

- `GET /health`: service status and whether Supabase environment settings exist.
- `POST /api/auth/register`: email/password account registration.
- `POST /api/auth/login`: email/password sign-in.
- `GET /api/me`: authenticated account/profile.
- `GET /api/tasks`: active tasks from the database.
- `GET /api/wallet`: pending/available ledger totals and recent entries.
- `POST /api/withdrawals`: intentionally returns 503 until payout approval and testing.

All account-specific routes require `Authorization: Bearer <access_token>`.

## Required environment

Copy `backend/.env.example` to a private environment configuration and set:

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`
- `PORT` (optional)
- `ALLOWED_ORIGINS` (optional comma-separated exact origins)

Never put the service-role key in the Android app, public Git, or a client-side build.

## Database setup

Apply `database/migrations/001_initial_schema.sql` to the intended Supabase project. The migration creates profiles, provider task catalogue, an append-only wallet ledger, a balance view, and withdrawal request storage. It does not create fake tasks, credit money, or enable withdrawals.

Configure Supabase Auth email confirmation and production password/security policies before public launch.

## Important launch gates

The API currently has no approved offer provider or payout integration. Task catalogue rows must only be populated from a provider contract that permits cash incentives in Egypt. Do not manually insert tasks that imply guaranteed payment.

Wallet writes must remain server-side. Provider callbacks need the exact signature-validation algorithm documented by the selected provider, idempotency handling, reversal handling, and reconciliation before any real reward is credited. Withdrawal creation and payment execution remain disabled until a real payout method has been approved and tested.
