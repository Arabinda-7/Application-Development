package com.example.allinone

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AllInOneApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        lateinit var instance: AllInOneApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        DataManager.initialize(this)
    }
}