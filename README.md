# Argent

[![CI](https://github.com/chinmay-tayade/argent-android/actions/workflows/ci.yml/badge.svg)](https://github.com/chinmay-tayade/argent-android/actions/workflows/ci.yml)

> A multi-module Android banking app, built to work through the parts of a
> payments client that are actually hard: sync under failure, transaction
> state you don't fully control, and credential storage that survives a
> stolen unlocked device.
>
> Backed by an in-repo mock server — clone and run, no accounts needed.

**Status: Milestone 0 complete — foundation in place, building green.**
The multi-module skeleton, convention plugins, `:core:domain` (with tests),
and a Hilt-wired `:app` are done. Features land incrementally from here
(see [ROADMAP](ROADMAP.md)). Every claim in this README maps to code that
exists before it leaves the "planned" list.

---

## Why this exists

Most open-source banking-app samples are online-only CRUD with a nice UI.
Argent is built around the failure cases instead:

- What happens when the user taps **Send** with no signal.
- How a transfer moves through states the client doesn't control (`PENDING`,
  server-side review, settlement, rejection).
- How you store a session such that an attacker with an unlocked device still
  can't drain an account.

## Architecture (target)

Multi-module, unidirectional data flow, domain layer with zero Android
dependencies. Full write-up: **[docs/architecture.md](docs/architecture.md)**.

```
:app                     composition root — navigation host, DI wiring
  ├─ :feature:auth  :feature:dashboard  :feature:accounts
  ├─ :feature:transactions  :feature:transfers  :feature:cards
  │      (feature modules depend down only, never on each other)
  ├─ :core:designsystem  :core:ui  :core:common  :core:testing
  ├─ :core:data          repositories — the only layer touching both sources
  │     ├─ :core:network     Retrofit, DTOs, error mapping, retry
  │     ├─ :core:database    Room, DAOs, entities
  │     └─ :core:datastore   encrypted preferences, session
  ├─ :core:domain        use cases + models — pure Kotlin, no Android
  └─ :core:security      Keystore, BiometricPrompt, certificate pinning
:sync                    WorkManager workers + the offline sync engine
:mock-server             Ktor — serves accounts, transactions, transfers
:benchmark  :baselineprofile
```

**Dependency rule (CI-enforced once `modulith` lands):** `:feature:*` →
`:core:domain` → nothing Android. `:core:data` is the only module importing
both `:core:network` and `:core:database`.

### What exists today (Milestone 0)

```
:app                 Hilt-wired Application + edge-to-edge Compose placeholder
:core:common         Outcome<T,E> result type · DI-provided coroutine dispatchers
:core:designsystem   ArgentTheme (Material 3, light/dark) — tokens come in M1
:core:domain         pure Kotlin — Money, Account, ValidateTransferAmount + tests
:core:testing        MainDispatcherRule, fixture builders
build-logic          convention plugins: application, library, library.compose,
                     feature, hilt, jvm.library
```

## The hard parts

| Topic | Doc |
|---|---|
| Offline transfer state machine — idempotency keys, optimistic UI, per-operation conflict resolution, backoff | [docs/offline-sync.md](docs/offline-sync.md) |
| Security — Keystore, BiometricPrompt + CryptoObject, certificate pinning, session hardening, the root-detection trade-off | [docs/security.md](docs/security.md) |
| Testing strategy — the pyramid, what's covered where, why hand-written fakes over a mocking library for repositories | [docs/testing.md](docs/testing.md) |

## Tech stack (target)

Kotlin · Jetpack Compose · Material 3 · Hilt · Room · DataStore · WorkManager ·
Paging 3 · Retrofit · OkHttp · kotlinx.serialization · Turbine · MockK ·
MockWebServer · Roborazzi · Macrobenchmark · Detekt · ktlint · GitHub Actions.
Mock server: Ktor.

## Running it

```
./gradlew build            # compiles everything, runs unit tests, lint, R8
./gradlew :app:assembleDebug
```

Then open in Android Studio and run the `app` configuration — you'll get the
Milestone 0 placeholder screen. Toolchain: AGP 8.9.1 · Kotlin 2.1.20 ·
Gradle 8.13 · JDK 17 · compileSdk 36 / minSdk 26. (A bump to the current
AGP 9.x / Kotlin 2.4.x line is the first task in Milestone 1.)

No secrets, no API keys, no real bank.

## Open-source acknowledgements

Architecture informed by [android/nowinandroid](https://github.com/android/nowinandroid)
(Apache-2.0) — module structure, convention-plugin pattern, test-double
strategy — and by the payments-flow structure in
[kickstarter/ios-oss](https://github.com/kickstarter/ios-oss) (Apache-2.0).
Both studied as references; no code copied. Dependencies are under their own
licenses.

## License

[Apache-2.0](LICENSE).

## Author

Chinmay Tayade — [LinkedIn](https://www.linkedin.com/in/chinmaytayade)
