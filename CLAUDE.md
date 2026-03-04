# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (signed with app/tel24.keystore)
./gradlew assembleRelease

# Install debug APK on connected device
./gradlew installDebug

# Run lint
./gradlew lint

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "bg.iag.tel24.SomeTest"
```

## Architecture

The app is a 3-tab + 1-search-tab telephone directory viewer using MVVM.

**Data flow:**
```
ApiService (Retrofit) → EmployeeRepository → ViewModel (LiveData) → Fragment → RecyclerView
                                ↕
                        Room (CacheDao/CachedData) — offline fallback
```

**Key design decisions:**
- `EmployeeRepository` wraps all network calls in `fetchTree`/`fetchSearch` — on success it caches JSON to Room; on failure it falls back to the cached JSON and returns `Result.Error(exception, cached)`.
- `TreeNode` serves double duty: both departments (`leaf=false`, has `children`) and employees (`leaf=true`). The `isEmployee` computed property handles the distinction.
- `TreeAdapter` flattens the recursive tree into a flat `List<Item>` with indentation levels, then filters it via `rebuild()` for expand/collapse without extra libraries.
- Employee photos are loaded with Coil and pass through `FaceCircleCropTransformation` (center-crop to circle with face detection heuristic).
- `EmployeeDetailSheet` passes `TreeNode` as a JSON string via `Bundle` arguments.
- The three tree tabs (ИАГ, РДГ, ДП) each use the same `TreeFragment` + `TreeViewModel` pair, distinguished by a `DataSource` enum argument.

**UI:**
- `MainActivity` hosts a `ViewPager2` + `TabLayout` with 4 tabs via `MainPagerAdapter`.
- Tabs 0–2: `TreeFragment` with `DataSource.IAG/RDG/DP`.
- Tab 3: `SearchFragment` — searches by name (first + last) or GSM number.
- Employee taps open `EmployeeDetailSheet` (BottomSheet), photo tap opens `PhotoViewerDialog`.

## API Endpoints

Base URL: `https://vasil.iag.bg/`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `tel/v7/iag_empl` | IAG org tree |
| GET | `tel/v7/rdg_empl` | RDG org tree |
| GET | `tel/v7/dp_dgs_empl` | DP org tree |
| GET | `all_empl/imeAndFam?strIme=&strFam=` | Search by name |
| GET | `all_empl/byGSM?number=` | Search by GSM |

Employee photos: `https://vasil.iag.bg/upload/{glavpod}/{pict}`

## UI Language

The app UI is in Bulgarian. Error messages and tab labels use Bulgarian text (e.g. `"Офлайн – кеширани данни"`, `"Грешка при зареждане"`).
