# Walkthrough - Suspend Function Call Fix

I have fixed the compilation error in `FinanceHistoryActivity.kt` where a `suspend` function was being called from a non-suspend context.

## Changes Made

### Finance History Activity

#### [FinanceHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/FinanceHistoryActivity.kt)

- Added missing imports: `androidx.lifecycle.lifecycleScope` and `kotlinx.coroutines.launch`.
- Updated `onResume()` to launch a coroutine using `lifecycleScope` to call `DataManager.loadData()`.
- Moved dependent UI update methods (`updateYearlyAnalytics()` and `updateDynamicBackground()`) inside the coroutine block to ensure they run after the data is successfully loaded.

```kotlin
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            DataManager.loadData(this@FinanceHistoryActivity)
            updateYearlyAnalytics()
            updateDynamicBackground()
        }
    }
```

## Verification Results

### Automated Tests
- Executed `:app:compileDebugKotlin` and the build finished successfully.

> [!NOTE]
> This fix ensures that the data loading process happens asynchronously without blocking the main thread and adheres to Kotlin's coroutine requirements.
