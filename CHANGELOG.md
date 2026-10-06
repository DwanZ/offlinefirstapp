# Changelog

All notable changes to **Insignia Empresarial** (Offline-First Financial Dashboard) are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- Excess top spacing from nested Scaffold + edge-to-edge (app + feature scaffolds use zero content insets; TopAppBar owns status-bar padding once)

### Added
- Compose `@Preview` samples for Home, Accounts, Account detail, Transactions, Sync, design system
- Portfolio screenshots under `docs/screenshots/`
- Architecture documentation in `docs/architecture.md`
- Typed `ValidationReason` / `UserMessageKey` with Android string resources (no hardcoded use-case error copy)
- Expanded unit tests for transfer use case and ViewModel message mapping

### Changed
- Split grouped use cases, repositories, models, entities, DAOs, DTOs, and design-system components into one type per file

## [0.6.1] - 2026-10-05

### Added
- Account detail screen with ledger slice and budget summary
- Offline transfer sheet (atomic Room debit/credit + outbox)
- ConnectivityObserver that enqueues sync when network returns

## [0.6.0] - 2026-10-03

### Added
- Unit tests for `AddTransactionUseCase` and `HomeViewModel` (MockK + Turbine)
- GitHub Actions workflow `pr-checks.yml` (lint, unit tests, assembleDebug)

## [0.5.0] - 2026-10-03

### Added
- Feature modules: Home, Accounts, Transactions, Sync health
- Bottom navigation + UDF `StateFlow<UiState>` ViewModels
- Offline add-transaction sheet with outbox-backed save

## [0.4.0] - 2026-10-03

### Added
- Room SSOT schema (accounts, transactions, budgets, sync_outbox, sync_meta)
- Retrofit mock API with latency and intermittent 500s
- Outbox write path + WorkManager `SyncWorker` (LWW by `updatedAt`)

## [0.3.0] - 2026-10-03

### Added
- `:core:designsystem` brand tokens (Emerald / Indigo / Cyan on Slate)
- Dark-first Material 3 theme + financial semantic colors
- Shared `MoneyText`, `MetricCard`, `SyncStatusChip`

## [0.2.0] - 2026-10-03

### Added
- Multi-module Clean Architecture skeleton (`:core:*`, `:feature:*`)
- Domain models, repository ports, use cases
- Hilt DI wiring in `:app`

## [0.1.0] - 2026-10-03

### Added
- Android Compose application scaffold (`:app`)
- AGP / Kotlin / Compose BOM baseline from Android Studio template
- Project `.gitignore` for local Gradle and IDE artifacts
- Initial `README.md` and versioned changelog
