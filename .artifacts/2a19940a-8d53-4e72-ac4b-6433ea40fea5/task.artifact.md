# Tasks - Student Fund Tracker Implementation

- [x] Project Setup & Dependencies
    - [x] Update `libs.versions.toml`
    - [x] Update project-level `build.gradle.kts`
    - [x] Update app-level `build.gradle.kts`
    - [x] Hilt Application setup
- [x] Data Layer
    - [x] Define Data Models (`Student`, `Payment`, `Expense`)
    - [x] Implement Room Entities and DAOs
    - [x] Setup Room Database
    - [x] Implement Firestore Data Service
- [x] Domain Layer
    - [x] Implement `FundRepository` with Early Bird logic
    - [x] Setup Hilt modules for DI
- [x] UI Layer - Foundation
    - [x] Setup Navigation (Screens & NavHost)
    - [x] Create UI components (Summary Cards, List Items)
- [x] UI Layer - Features
    - [x] Dashboard Screen
    - [x] Student List & Detail Screens
    - [x] Expense Tracker Screen
    - [x] Add/Edit Dialogs
- [x] Features
    - [x] CSV Import (SAF)
    - [x] CSV/PDF Export & Share
- [x] Verification
    - [x] Unit Tests for pricing logic
    - [x] Manual UI verification

- [x] Student List Seeding & UI Enhancements
    - [x] Seed student list in `FundRepository`
    - [x] Remove CSV import logic from `StudentViewModel` and `ImportExportScreen`
    - [x] Add color effects and discount badges to `StudentListScreen`

- [x] High-Fidelity UI Refinement
    - [x] Update `Color.kt` and `Theme.kt` with Indigo palette
    - [x] Add icons and refined styling to Dashboard
    - [x] Refine Student List with bold chips and progress bars

- [x] High-Fidelity Student List Overhaul (Mockup Match)
    - [x] Update `StudentViewModel` with status filtering and counts
    - [x] Implement Blue Header with integrated Search Bar
    - [x] Implement Status Filter Chips with numeric counts
    - [x] Redesign `StudentItem` (Avatar, Pill, Thick Progress Bar, Percentage)
