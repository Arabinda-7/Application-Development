package com.example.allinone.data

import android.content.Context
import com.example.allinone.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupDataManager: Manages data persistence, loading, saving, and backup operations.
 */
@Singleton
class BackupDataManager @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Main)

    fun loadData(context: Context) {
        scope.launch {
            DataManager.refreshLegacyState(context)
        }
    }

    fun saveData(context: Context) {
        // Data is saved automatically via Room repositories
    }
}
