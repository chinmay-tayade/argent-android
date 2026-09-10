# Roadmap

Built in public, incrementally. Each item is a small set of commits ending
with something that builds and (once CI exists) passes. Dates are targets,
not promises — an honest slower cadence beats backdated commits.

## Milestone 0 — foundation ✅

- [x] Design docs: architecture, offline-sync, security, testing
- [x] Gradle skeleton: `settings.gradle.kts`, version catalog, root build,
      `build-logic/convention` with `argent.android.application` / `.library` /
      `.library.compose` / `.feature` / `.hilt` / `argent.jvm.library` plugins
- [x] `:core:common` (Outcome type, DI dispatchers), `:core:designsystem`
      (ArgentTheme), `:core:testing` (MainDispatcherRule, fixtures) compile
- [x] `:core:domain` compiles — pure Kotlin/JVM, `Money`, `Account`,
      `ValidateTransferAmount` with 12 passing unit tests
- [x] `:app` assembles (debug + release/R8), Hilt wired, Compose placeholder
- [x] `./gradlew build` green locally (AGP 8.9.1 / Kotlin 2.1.20 / Gradle 8.13)
- [x] CI: `assembleDebug` + unit tests + `lintDebug` on every PR / push to main

## Milestone 1 — auth + read path

- [ ] Bump toolchain to current (AGP 9.x / Kotlin 2.4.x) — do this first,
      interactively in Android Studio; it's a known follow-up, not a blocker
- [ ] Wire Detekt + ktlint into the convention plugins + CI
- [ ] `:mock-server` (Ktor) serves `/accounts`, `/transactions`
- [ ] `:core:network` — Retrofit, DTOs, typed error mapping, retry policy
- [ ] `:core:database` — Room schema, DAOs, `Flow` queries
- [ ] `:core:data` — `AccountsRepository`, `TransactionsRepository`
- [ ] `:core:security` — Keystore wrapper, `BiometricPrompt` + `CryptoObject`
- [ ] `:feature:auth` — biometric + PIN fallback, session timeout
- [ ] `:feature:dashboard`, `:feature:accounts` — Flow-driven Compose UI
- [ ] `:feature:transactions` — Paging 3 + `RemoteMediator`
- [ ] Unit tests: domain use cases, ViewModels (Turbine + hand-written fakes)

## Milestone 2 — the signature feature

- [ ] `:sync` — operation queue, `SyncEngine`, WorkManager workers
- [ ] Offline transfer: `PENDING` persistence, client `opId`, optimistic UI
- [ ] Exponential backoff + jitter + attempt ceiling
- [ ] Conflict resolution — per operation type, not one global rule
- [ ] Compose UI test: submit transfer offline → row shows `PENDING` →
      connectivity restored → settles to `CONFIRMED`
- [ ] `docs/offline-sync.md` updated to match the implementation exactly

## Milestone 3 — hardening + release

- [ ] Certificate pinning (`OkHttp CertificatePinner`)
- [ ] Beneficiaries CRUD with domain validation
- [ ] Card controls (freeze/unfreeze) — optimistic toggle with rollback
- [ ] Statements — background PDF generation via WorkManager
- [ ] `FLAG_SECURE` on sensitive screens; log/switcher redaction
- [ ] Screenshot tests (Roborazzi) for the design system + key screens
- [ ] Instrumented test job added to CI (emulator)
- [ ] Tag `v0.1`

## Milestone 4 — performance (feeds `android-perf-lab`)

- [ ] `:baselineprofile` module + generated profile
- [ ] `:benchmark` — Macrobenchmark startup test, fixed device, 10 iterations
- [ ] Fix one real over-recomposition path; document the before/after
- [ ] Add a transactions-query index; capture `EXPLAIN QUERY PLAN` delta

## Later

- [ ] Extract `:sync` into a standalone `offline-sync-engine` library
- [ ] Multi-currency display + FX
- [ ] Balance widget (Glance) + Wear tile
