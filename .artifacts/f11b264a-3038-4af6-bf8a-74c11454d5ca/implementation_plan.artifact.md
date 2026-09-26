# Implementation Plan - Global White Background & Black Text Styling

Update all screens (`StudentListScreen`, `StudentDetailScreen`, `ExpenseScreen`, `ImportExportScreen`) and dialogs to have a clean, consistent **white background** and **black text** styling across the app.

## User Review Required

> [!IMPORTANT]
> - All screen backgrounds, card surfaces, and list containers will be styled with pure white backgrounds (`Color.White`).
> - All textual elements across headers, lists, cards, and dialogs will be styled in black (`Color.Black` or dark text).

## Proposed Changes

### UI Screens & Dialogs (`StudentListScreen.kt`, `StudentDetailScreen.kt`, `ExpenseScreen.kt`, `ImportExportScreen.kt`, `Dialogs.kt`)
- **[MODIFY] [StudentListScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/StudentListScreen.kt)**
  - Ensure all list containers, cards, headers, and texts use white backgrounds and black text.
- **[MODIFY] [StudentDetailScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/StudentDetailScreen.kt)**
  - Ensure detail screen background, payment history cards, clothing order cards, and texts are white background with black text.
- **[MODIFY] [ExpenseScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/ExpenseScreen.kt)**
  - Style expense screen background white, expense cards white background with black text.
- **[MODIFY] [ImportExportScreen.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/ImportExportScreen.kt)**
  - Style reports screen background white, cards white with black text.
- **[MODIFY] [Dialogs.kt](file:///C:/Users/rm/AndroidStudioProjects/TEAM4/app/src/main/java/com/example/team4/ui/screen/Dialogs.kt)**
  - Ensure all dialog backgrounds and text are white background and black text.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to ensure clean compilation.

### Manual Verification
- Deploy to emulator/device, verify that every screen (Students, Expenses, Reports, Student Detail) has a white background and black text.
