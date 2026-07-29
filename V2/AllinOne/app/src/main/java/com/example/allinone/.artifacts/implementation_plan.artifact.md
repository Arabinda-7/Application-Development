# Performance & UI Polish Plan

This plan addresses a specific UI glitch, performance risks in finance calculations, and modernizes deprecated Activity transitions.

## User Review Required

> [!IMPORTANT]
> **Finance Performance**: I will be adding new SQL queries to `AppFinanceDao` to fetch monthly totals directly. This significantly reduces memory usage compared to filtering thousands of transactions in memory.

## Proposed Changes

### 🎨 1. Dashboard UI Polish
#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/HomeScreen.kt)
- Update "Safe Spend" amount color logic:
    - Use `Color.Red` when balance is negative.
    - Keep `Color(0xFF2EC4B6)` (Cyan) when balance is positive.

---

### 🚀 2. Finance Calculation Performance
#### [MODIFY] [GlobalDaos.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/database/GlobalDaos.kt)
- Add `@Query` to `AppFinanceDao` to calculate `sum(amount)` for a specific `type` and time range (month/year) directly in SQLite.

#### [MODIFY] [FinanceRepository.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/FinanceRepository.kt)
- Expose new SQL-based calculation methods.

#### [MODIFY] [FinanceDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/FinanceDataManager.kt)
- Delegate calculation calls to the repository instead of filtering the global `transactions` list.

---

### 📜 3. Modernize Activity Transitions
#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
#### [MODIFY] [LockActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LockActivity.kt)
#### [MODIFY] [ProfileActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProfileActivity.kt)
- Replace `overridePendingTransition(enter, exit)` with the modern `overrideActivityTransition` (for API 34+) or use `ActivityOptionsCompat` for backward compatibility.

## Verification Plan

### Manual Verification
1. **Financial Indicator**: Log an expense that exceeds the budget. Verify that the "Safe Spend" text turns Red on the home screen.
2. **Performance**: Verify dashboard loads instantly even with simulated high transaction volume (if possible).
3. **Transitions**: Navigate between Profile and Main screen, and through the Lock screen. Verify animations still trigger correctly.
