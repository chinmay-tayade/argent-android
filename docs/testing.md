# Testing strategy

## The pyramid, concretely

| Level | Where | Tools | What is actually asserted |
|---|---|---|---|
| Unit — domain | `:core:domain` | JUnit5, kotlin.test, kotlinx-coroutines-test | Use cases, validators, the transfer state-machine reducer (every transition in `offline-sync.md`), `Backoff` |
| Unit — presentation | `:feature:*` | Turbine over `StateFlow`, hand-written fake repositories | For each `Event`: the exact `State` sequence and `Effect`s emitted |
| Integration — data | `:core:data` | Room in-memory, `MockWebServer` | Repository ↔ Room ↔ network round trips; refresh writes cache; `Flow` emits after write |
| Integration — sync | `:sync` | `MockWebServer` scripted (`5xx` → `2xx`) | Row ends `CONFIRMED`; attempt count correct; `Idempotency-Key` stable across retries |
| UI | `:feature:*` | Compose UI test, `createAndroidComposeRule`, semantics | The four critical flows: login, view balance, make transfer, **offline transfer shows Pending then settles** |
| Screenshot | `:core:designsystem`, key screens | Roborazzi | Components + key screens, light and dark |
| Macro | `:benchmark` | Macrobenchmark | Cold-start time; scroll jank on the transaction list |

## Coverage targets

- `:core:domain` and `:core:data`: **80%+ line coverage**, and the number is
  published via Kover in the CI PR comment. This is where the logic that
  matters lives.
- `:feature:*` ViewModels: high coverage of the reducer, lighter on the glue.
- Compose UI: the critical flows only — UI tests are slow and brittle past a
  point; the design-system screenshot tests carry visual regressions.
- No overall-percentage vanity target. A green suite you trust beats a high
  number you don't.

## Why hand-written fakes, not a mocking library, for repositories

Following the `nowinandroid` approach:

- A `FakeTransfersRepository` that actually stores transfers in a `MutableList`
  and emits a real `Flow` exercises the ViewModel against *behaviour*, not a
  script of stubbed return values.
- Mock setups (`every { repo.observe() } returns flowOf(...)`) drift from
  reality and turn into change-detector tests — they break on refactors that
  didn't change behaviour, and pass on changes that did.
- MockK is still used for narrow cases: verifying a side effect was invoked
  (e.g. "analytics event fired"), or faking a platform type that's painful to
  fake by hand.

## Test data

A `fixtures` module in `:core:testing` with builders:

```kotlin
fun transfer(
  state: TransferState = TransferState.Confirmed,
  amount: Money = Money(1000, "GBP"),
  ...
): Transfer
```

Small DSL for scripting `MockWebServer` responses so sync tests read as
scenarios, not as `enqueue(MockResponse()...)` noise.

## What "done" looks like for a feature PR

1. Domain logic has unit tests covering the happy path + each failure branch.
2. The ViewModel has a Turbine test per `Event`.
3. If it touches persistence or network, an integration test.
4. If it's one of the four critical flows, the UI test is updated.
5. `./gradlew check` green locally; CI green.
