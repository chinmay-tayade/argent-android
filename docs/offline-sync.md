# Offline-first & the transfer state machine

This is the feature the whole repo exists to demonstrate. It will be
extracted into a standalone `offline-sync-engine` library once it's stable
here.

## The UX contract

`PENDING` is a **feature**, not an error. A user who initiates a transfer
offline gets immediate, honest feedback: the transfer appears in the list,
visibly marked pending, with a clear affordance. The app never pretends the
transfer succeeded, and never blocks the user waiting for a network round
trip it can't guarantee.

## State machine

```
          submit (validated locally)
                 │
                 ▼
            ┌─────────┐   enqueued, waiting for connectivity / worker slot
            │ PENDING │
            └────┬────┘
                 │ worker picks it up
                 ▼
            ┌─────────┐
            │ SYNCING │
            └────┬────┘
     ┌───────────┼─────────────┬───────────────┐
     ▼           ▼             ▼               ▼
┌─────────┐ ┌─────────┐  ┌───────────┐   ┌──────────┐
│CONFIRMED│ │ FAILED  │  │ CONFLICT  │   │ (retry)  │
│         │ │(terminal)│  │(needs UI) │   │→ PENDING │
└─────────┘ └─────────┘  └───────────┘   └──────────┘
```

Transitions:

| From | Event | To |
|---|---|---|
| — | `submit`, local validation passes | `PENDING` |
| `PENDING` | worker starts, network available | `SYNCING` |
| `SYNCING` | `2xx` + server id returned | `CONFIRMED` |
| `SYNCING` | `409 Conflict` | `CONFLICT` |
| `SYNCING` | `4xx` (validation, insufficient funds server-side) | `FAILED(reason)` |
| `SYNCING` | `5xx` / IO, attempts < ceiling | `PENDING` (after backoff) |
| `SYNCING` | `5xx` / IO, attempts == ceiling | `FAILED(exhausted)` |
| `CONFLICT` | user resolves | `PENDING` or `FAILED` per choice |

`FAILED` and `CONFIRMED` are terminal. `CONFIRMED` transfers are reconciled
against the server's transaction list on the next full sync (belt and
braces).

## Idempotency

Every mutating operation carries a client-generated `opId` (UUID v4),
persisted with the row before any network attempt.

- The `opId` is sent as an `Idempotency-Key` header.
- The mock server records processed keys and returns the original result for
  a repeat — so a retry after a response we never received is safe.
- This is what makes "at-least-once delivery + server-side dedupe" work, and
  it's why we don't need (and can't truly get) exactly-once.

## The sync engine

```
:sync
├── SyncOperation           persisted: opId, type, payloadJson, attempts,
│                           nextAttemptAt, state
├── OperationQueue          Room-backed; FIFO within a type, types independent
├── SyncEngine              drains the queue; calls the right handler per type
├── operation handlers      CreateTransferHandler, UpdateCardHandler, ...
│                           each knows its endpoint + its conflict strategy
├── Backoff                 exponential, full jitter, cap
└── workers                 SyncWorker (CoroutineWorker), constrained to
                            NetworkType.CONNECTED, unique work per type
```

### Backoff

`delay = random(0, min(cap, base * 2^attempt))` — "full jitter". Base 2s,
cap 5min, ceiling 8 attempts. Jitter matters: without it, every device that
went offline during an outage retries in lockstep when the network returns
and stampedes the server.

### Conflict resolution — per operation type

There is no single global rule. Each handler declares its strategy:

- **CreateTransfer** — `409` means the server already has this `opId`
  (duplicate) → treat as success, adopt the server's record. A *true*
  conflict (e.g. beneficiary was deleted server-side) → `FAILED`, surface it.
- **UpdateCardControls** — last-write-wins is acceptable; re-send with the
  latest local intent.
- **UpdateBeneficiary** — server-wins on the disputed fields, but keep a
  local copy and prompt the user to re-apply their change.

### Connectivity

`ConnectivityObserver` wraps `ConnectivityManager.NetworkCallback` as a
`Flow<Status>`. The engine uses it to (a) let WorkManager's constraint do the
heavy lifting and (b) proactively kick a sync when connectivity returns
rather than waiting for the next scheduled window.

## What the UI sees

```
Repository.observeTransfers(): Flow<List<Transfer>>   // from Room, always
```

The row renders from `transfer.state`. Pending rows are de-emphasised with a
retry / cancel affordance. When the engine updates a row's state, Room emits,
the `Flow` re-collects, Compose recomposes. The UI has no direct knowledge of
the sync engine.

## Testing this

- **Unit:** the state machine's transition table, as a pure function
  `reduce(state, event): state`. Every row above is a test case.
- **Unit:** `Backoff` — determinism with a seeded RNG, cap and ceiling
  behaviour.
- **Integration:** `SyncEngine` against `MockWebServer` scripted to return
  `5xx` then `2xx`; assert the row ends `CONFIRMED` with the right attempt
  count; assert the `Idempotency-Key` header is stable across retries.
- **UI:** Compose test — submit offline (no server), assert `PENDING` row;
  start the server, trigger sync, assert it settles.

## Known limitations (v0.1)

- No cross-device sync of pending operations (they live on the origin device).
- Conflict UI is minimal — a dialog, not a merge view.
- The queue is not encrypted at rest yet (payloads contain amounts and
  beneficiary ids) — tracked in ROADMAP Milestone 3.
