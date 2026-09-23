# Implementation Plan - Clear/Delete All Students Feature

Add a feature to clear/delete all students from the database and UI, providing a button in the Student List screen with a confirmation dialog.

## User Review Required

> [!IMPORTANT]
> This will permanently delete all student records (and optionally their associated payments) from the local Room database and Firestore.

## Proposed Changes

### Data Layer (`FundDao.kt`, `FundRepository.kt`, `FirestoreService.kt`)
- **[MODIFY] [FundDao.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/data/local/FundDao.kt)**
  - Add `@Query("DELETE FROM students") suspend fun deleteAllStudents()`
- **[MODIFY] [FundRepository.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/data/repository/FundRepository.kt)**
  - Add `suspend fun deleteAllStudents()` to clear local table and remove students from Firestore.

### ViewModel Layer (`StudentViewModel.kt`)
- **[MODIFY] [StudentViewModel.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/viewmodel/StudentViewModel.kt)**
  - Add `fun deleteAllStudents()` function delegating to repository.

### UI Layer (`StudentListScreen.kt`)
- **[MODIFY] [StudentListScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/StudentListScreen.kt)**
  - Add a "Delete All" / "Clear All Students" option in the Student List screen header or overflow menu.
  - Show a confirmation dialog ("Are you sure you want to delete all students?").

## Verification Plan

### Automated Tests
- Build project successfully using Gradle (`app:assembleDebug`).

### Manual Verification
- Deploy to emulator/device, navigate to Students tab, click "Clear All Students", confirm the action, and verify the student list becomes empty (0 students).
