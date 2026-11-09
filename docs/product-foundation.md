# Earn Money — Product Foundation

## Product goal
An Android rewards application owned by the publisher. Users may earn rewards only from verified activities supplied by providers that explicitly permit cash-reward incentives.

## First release scope
- Account registration and sign-in.
- Offer/activity catalogue supplied by an approved provider.
- Clear activity terms and reward status.
- Wallet with separate Pending and Available balances.
- Transaction history.
- Withdrawal request flow, enabled only after payout operations are verified.
- Admin review and basic fraud controls.

## Non-negotiable money rules
1. Never credit a withdrawable balance from a client-side event.
2. A reward stays Pending until the provider's server-to-server confirmation is validated.
3. Process provider callbacks idempotently; one provider transaction can be credited only once.
4. Support reversals and chargebacks with an auditable ledger.
5. Never place provider secrets, signing keys, or database credentials in the APK or Git.
6. Do not reward users for clicking ads or viewing ads unless the provider's written terms explicitly allow cash incentives. Standard AdMob ads must not be used as cash-reward inventory.
7. Do not promise a fixed income. Revenue depends on approved provider inventory, eligible users, conversion, fees, and payout terms.

## Provider approval gate
Before real rewards or withdrawals are enabled, confirm in writing:
- Publisher account approval.
- Egypt availability and actual Egyptian user inventory.
- Cash-incentive permission for each activity type.
- Publisher payout method, threshold, fees, and settlement timing.
- Callback authentication and reversal behavior.
- At least one successful end-to-end test and a received publisher payout.

## Initial technical direction
- Android: Kotlin + Jetpack Compose.
- API: Node.js, starting with a minimal health endpoint.
- Database: PostgreSQL after selecting the hosting/provider arrangement.
- Admin: separate protected interface, not embedded privileged controls in the APK.

## Current implementation status
This repository currently contains a foundation only. No live offer provider, real wallet, payment processing, user authentication, or cash withdrawals are connected. Any demo data must be clearly labelled as test data.
