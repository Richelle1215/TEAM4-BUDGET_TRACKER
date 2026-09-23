# Implementation Plan - Student Management Refinements & UI Styling

Implement user requests:
1. Remove the floating action button for adding students.
2. Allow editing student payment records (similar to expenses) in addition to student names.
3. Make the background white and all text black in the student list.

## User Review Required

> [!IMPORTANT]
> The add student button is removed since students are pre-seeded via the definitive student list. Payment history items will now feature an edit button opening `EditPaymentDialog`.

## Proposed Changes

### Data & Domain Layer (`FundRepository.kt`)
- **[MODIFY] [FundRepository.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/data/repository/FundRepository.kt)**
  - Add `suspend fun updatePayment(payment: Payment)`

### ViewModel Layer (`StudentViewModel.kt`)
- **[MODIFY] [StudentViewModel.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/viewmodel/StudentViewModel.kt)**
  - Add `fun updatePayment(payment: Payment)`

### Dialogs (`Dialogs.kt`)
- **[MODIFY] [Dialogs.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/Dialogs.kt)**
  - Add `EditPaymentDialog`

### UI Screens (`StudentListScreen.kt`, `StudentDetailScreen.kt`)
- **[MODIFY] [StudentListScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/StudentListScreen.kt)**
  - Remove `floatingActionButton` for adding students.
  - Set Scaffold container color and background to pure white (`Color.White`), and ensure all text is black.
- **[MODIFY] [StudentDetailScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/StudentDetailScreen.kt)**
  - Add edit action button to payment items and integrate `EditPaymentDialog`.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify compilation.

### Manual Verification
- Deploy to emulator/device, verify student list background is white with black text, check that add student FAB is gone, and test editing student names and payment records.
