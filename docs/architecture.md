# Architecture

Insignia Empresarial is a modular **offline-first** Android app. Compose screens never talk to the network; they observe Room. Mutations write locally and enqueue an outbox row; WorkManager syncs when connectivity allows.

## Module boundaries

```
:app                  → Application, Hilt, NavHost, theme host
:feature:*            → UI + ViewModel + UiState (depends on domain + designsystem)
:core:domain          → entities, repository ports, use cases (pure Kotlin)
:core:data            → repository impls, mappers, sync orchestrator
:core:database        → Room SSOT + sync_outbox
:core:network         → Retrofit + mock interceptor
:core:sync            → WorkManager + connectivity trigger
:core:designsystem    → brand tokens + shared composables
:core:common          → Outcome, AppError, SyncStatus, dispatchers
```

Rules:

- `:core:domain` has **zero** Android dependencies.
- Features never import Room entities or Retrofit DTOs.
- Data wiring lives in `:app` (`AppModule`) and `:core:data`.

## Offline write path

1. User action → ViewModel → UseCase → Repository.
2. Room `@Transaction`: update domain tables + insert `sync_outbox`.
3. UI updates immediately via `Flow` from DAOs.
4. `SyncScheduler` enqueues unique WorkManager work `insignia_sync`.
5. Worker pulls remote changes (LWW by `updatedAt`), then drains outbox FIFO.
6. Success → mark `SYNCED` / clear outbox; retryable failure → WorkManager backoff; terminal failure → `FAILED` for user retry.

## Conflict policy (v1)

**Last-write-wins** using `updatedAtEpochMs`. Documented on the Sync screen and in code comments. A future version can add field-level merge or server version vectors.

## Design system

Dark-first Slate surfaces with Emerald / Indigo / Cyan brand accents and financial semantics (profit / loss / pending). Tokens live in `:core:designsystem`; features must not hardcode hex values.

## Swapping mock → real API

1. Keep `InsigniaApi` contracts.
2. Remove or gate `MockInterceptor` in `NetworkFactory`.
3. Point Retrofit `baseUrl` at your backend.
4. Keep Room + outbox unchanged — that is the offline contract.

## Screens

| Screen | Module | Notes |
|---|---|---|
| Home | `:feature:home` | Net worth, pending chip, pull-to-refresh sync |
| Accounts | `:feature:accounts` | Offline account list |
| Account detail | `:feature:accounts` | Ledger, budgets, transfer sheet |
| Transactions | `:feature:transactions` | Filters + offline add |
| Sync health | `:feature:sync` | Queue, errors, retry |

## Testing & CI

- Unit tests: use cases + ViewModels (MockK, Turbine).
- PR gate: `.github/workflows/pr-checks.yml` runs lint, unit tests, `assembleDebug`.
