# Technical Roadmap

## Phase 1 — Foundation
- Keep development on `setup/initial-foundation`.
- Establish Android and API project structure.
- Add CI checks and documented local setup.
- Do not modify `main`.

## Phase 2 — Provider feasibility
- Apply to a cash-incentive-compatible offer/survey provider.
- Verify Egypt inventory and publisher payouts before integrating.
- Record approved activity categories and provider callback specification.

## Phase 3 — Secure accounts and ledger
- Add authentication and PostgreSQL migrations.
- Implement an append-only wallet ledger.
- Add idempotent, authenticated provider webhook handling.
- Keep Pending and Available balances separate.

## Phase 4 — User experience
- Build home, offers, activity details, wallet, history, and withdrawal screens.
- Label test mode clearly until provider approval and payout verification.

## Phase 5 — Admin, fraud, and release
- Add protected admin review, rate limits, abuse detection, and audit logs.
- Test reversals, duplicate callbacks, invalid signatures, and account abuse.
- Review privacy disclosures and Google Play requirements.
- Release APK/internal testing first, then submit to Google Play.

## Release blockers
No cash withdrawal should be enabled until provider approval, callback validation, ledger reconciliation, and a real publisher payout have all been verified.
