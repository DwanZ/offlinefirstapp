# Insignia Empresarial — Offline-First

Public portfolio app by [DwanZ](https://github.com/DwanZ): a **financial dashboard** built with Clean Architecture, Jetpack Compose, and an offline-first Single Source of Truth.

> Sibling portfolio project: [archMigrationExample](https://github.com/DwanZ/archMigrationExample) (MVP → MVVM → MVI → Compose).

## Goals

- Modular Clean Architecture (`:core:*`, `:feature:*`)
- Compose UI with MVVM + Unidirectional Data Flow (`StateFlow<UiState>`)
- Room + Retrofit offline-first sync (outbox + WorkManager)
- Brand design system (Emerald / Indigo / Cyan on Slate surfaces)
- Unit tests (MockK) + GitHub Actions on PRs

## Current version

**v0.1.0** — Compose scaffold bootstrap. See [CHANGELOG.md](CHANGELOG.md) for the versioned history.

## Stack (target)

| Layer | Tech |
|---|---|
| UI | Jetpack Compose, Material 3, Navigation |
| DI | Hilt |
| Local | Room (SSOT + sync outbox) |
| Remote | Retrofit + OkHttp mock API |
| Sync | WorkManager |
| Tests | MockK, Coroutines Test, Turbine |

## Getting started

```bash
git clone https://github.com/DwanZ/offlinefirstapp.git
cd offlinefirstapp
```

Open in Android Studio, sync Gradle, run the `app` configuration.

## License

Apache-2.0 (to be added with later milestones if required).
