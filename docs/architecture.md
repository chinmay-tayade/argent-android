# Architecture

## Goals

1. **Domain logic is testable without Android.** `:core:domain` is a plain
   Kotlin/JVM module. If it ever needs `android.*`, something is in the wrong
   place.
2. **Feature isolation.** A feature module can be developed, tested, and
   reasoned about without knowing another feature exists.
3. **One source of truth per screen.** The UI renders a single immutable
   state object. Everything else is an event into the ViewModel.
4. **The network is never the UI's data source.** Reads come from the local
   database as a `Flow`; the network's job is to keep that database fresh.

## Module graph

```
                         ┌─────────┐
                         │  :app   │  composition root only
                         └────┬────┘
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
 ┌─────────────┐      ┌──────────────┐      ┌──────────────┐
 │ :feature:*  │ ...  │  :feature:*  │ ...  │  :feature:*  │
 └──────┬──────┘      └──────┬───────┘      └──────┬───────┘
        │  features depend DOWN only, never sideways │
        └─────────────────────┬─────────────────────┘
                              ▼
                      ┌───────────────┐
                      │  :core:data   │  repositories
                      └──────┬────────┘
             ┌───────────────┼────────────────┐
             ▼               ▼                ▼
     ┌─────────────┐  ┌────────────┐  ┌───────────────┐
     │:core:network│  │:core:      │  │ :core:datastore│
     │             │  │  database  │  │                │
     └─────────────┘  └────────────┘  └───────────────┘
                              ▼
                     ┌────────────────┐
                     │  :core:domain  │  pure Kotlin — models, use cases
                     └────────────────┘
                              ▼
             :core:common   :core:designsystem   :core:testing
```

Cross-cutting: `:core:security` is depended on by `:core:data` (encrypted
storage) and `:feature:auth` (biometric). `:sync` depends on `:core:data`
and `:core:domain`.

### The dependency rule

- `:feature:*` may depend on any `:core:*` **except** `:core:network`,
  `:core:database`, `:core:datastore` directly — it goes through `:core:data`.
- `:feature:*` must **not** depend on another `:feature:*`. Shared UI goes to
  `:core:designsystem` or `:core:ui`; shared logic goes to `:core:domain`.
- `:core:domain` depends only on `:core:common`. No Android, no other core.
- `:core:data` is the single module that wires `:core:network` +
  `:core:database` together.

Until the `modulith` convention plugins land, this is enforced by review.
After, a Gradle task walks the project graph and fails the build on any
violating edge, printing the offending `project(":x")` line.

## Data flow — "make a transfer"

```
TransferScreen (stateless)
   │  user fills form, taps Send
   ▼
TransferViewModel
   │  reduces Event.Submit → validates via domain use case
   │  emits State(submitting = true)
   ▼
MakeTransferUseCase           (:core:domain — pure)
   │  business rules: sufficient funds, valid beneficiary, limits
   ▼
TransferRepository            (:core:data)
   │  writes Transfer(state = PENDING, opId = UUID) to Room
   │  enqueues SyncOperation
   ▼
Room emits updated Flow<List<Transfer>>
   ▼
TransferViewModel collects → emits State(submitting = false, result = Pending)
   ▼
TransferScreen recomposes → shows the transfer as "Pending"
```

The ViewModel never calls the network. `TransferRepository` never returns a
network response to the caller — it returns after the local write. Settlement
happens later, through `:sync`, and reaches the UI as another `Flow` emission.

## State management

- **Pattern:** MVI / UDF. Each feature defines `State` (immutable data class),
  `Event` (sealed — user intents), `Effect` (sealed — one-shot: navigation,
  snackbars).
- **Holder:** `ViewModel` exposes `StateFlow<State>` and a `Channel<Effect>`
  consumed as a `Flow`. `onEvent(Event)` is the only entry point.
- **Collection:** `collectAsStateWithLifecycle()` in Compose.
- **Process death:** `SavedStateHandle` holds the minimum to rebuild state
  (selected ids, form drafts), not the whole state object.

### Why MVI over plain MVVM here

Payments flows have many transient sub-states (validating, submitting,
pending, needs-confirmation, failed-retryable, failed-terminal). Modelling
those as one sealed `result` field on an immutable state, mutated only by a
reducer, makes the impossible states unrepresentable and the transitions
testable in isolation. The cost is boilerplate; for this domain it pays off.
`:feature:dashboard` — mostly read-only — uses a lighter version with no
`Effect` channel.

## Dependency injection

Hilt. Scoping:

- `@Singleton` — repositories, the database, OkHttp/Retrofit, `SyncEngine`.
- `@ViewModelScoped` — nothing yet; added only if a dependency's lifetime
  genuinely matches a ViewModel's.
- Feature modules contribute their bindings via `@Module @InstallIn`.

Compile-time validation is the reason for Hilt over a runtime DI container
here — the graph will be large enough that catching a missing binding at
build time is worth the KAPT/KSP cost. (`basis-kmp` uses Koin because it
needs multiplatform.)

## Error handling

- Network → `:core:network` maps HTTP/IO failures into a sealed
  `NetworkError` (`Unauthorized`, `RateLimited`, `Server`, `Connectivity`,
  `Serialization`, `Unknown`).
- `:core:data` maps `NetworkError` + persistence failures into a domain-level
  `DataError`.
- Use cases return `Result<T, DomainError>` (a small sealed `Result` type, not
  `kotlin.Result`) so callers must handle failure.
- The ViewModel maps `DomainError` to a user-facing message + whether a retry
  affordance is shown.

No exceptions cross a module boundary as control flow.

## Caching

- Room is the cache. Every remote read is written to Room; the UI observes
  Room.
- Freshness: each cacheable entity carries `fetchedAt`. Repositories expose
  `observeX(): Flow` (always from Room) and `refreshX(): Result` (hits the
  network, writes Room). Screens call `refresh` on load / pull-to-refresh;
  the `Flow` does the rest.
- No time-based eviction in v0.1 — documented as a known limitation.
