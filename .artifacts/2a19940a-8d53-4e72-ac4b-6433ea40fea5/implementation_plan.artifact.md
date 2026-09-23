# Implementation Plan - Full App UI Overhaul (Mockup Match)

Overhaul all screens (Dashboard, Student List, Student Detail, Expenses, Reports) to match the high-fidelity screenshots provided by the user.

## User Review Required

> [!IMPORTANT]
> This is a major visual update that will replace almost all current Compose UI code with highly specific layouts, custom shapes, and complex components (like segmented progress bars and charts).

## Proposed Changes

### 1. Theme & Color Palette
- Define specific mockup colors in `Color.kt`:
    - `CollectionCard`: Deep Indigo
    - `ExpenseCard`: Soft Coral/Pink
    - `BalanceCard`: Light Teal/Green
    - `TargetCard`: Lavender/Grey
    - Segmented bar colors (Green, Orange, Red).

### 2. Dashboard Overhaul
- **Branded Header**: "Fund Tracker" with sync status badge.
- **Top Metrics Grid**: 4 custom cards with large amounts, subtitles, and icons.
- **Student Status Section**:
    - 3-column stats for Paid/Partial/Unpaid.
    - **Custom Segmented Progress Bar**: Multi-color bar representing the ratio of student statuses.
- **Line Chart**: Implement a custom `Canvas`-based line chart for "Collection vs. Spending" to avoid large dependencies.

### 3. Student Detail Redesign
- **Status-Dynamic Header**: The top background color changes based on status (Paid = Green, Partial = Orange, Unpaid = Red).
- **Circular Initials Avatar**: Large centered avatar.
- **Status Chips**: Matching mockup pills.
- **Progress Card**: Floating white card with thick progress bar and remaining amount text.
- **Timeline History**: Timeline-style list for payment history.

### 4. Expense Tracker Overhaul
- **Header Summary**: Red header with "Expense Tracker", total amount, and transaction count.
- **Category Chips**: Scrollable summary chips for Supplies, Events, Transport, etc.
- **Add Expense Dialog**: Redesign as a modern bottom-sheet style dialog with category selection icons.

### 5. Reports & Sync Overhaul
- **Sync Status Card**: Prominent "Sync Active" card with "Sync Now" button.
- **Summary List**: Clean row-based financial metrics.
- **Status Cards**: Grid of status counts.
- **Export List**: Items with icons and download (trailing) icons.

## Verification Plan

### Manual Verification
- Deploy to emulator/device and perform a screen-by-screen comparison with the provided images.
- Verify that status-based dynamic coloring (Student Detail) works correctly.
- Ensure the line chart correctly plots data points from the repository.
- Verify the segmented progress bar accurately represents student status ratios.
