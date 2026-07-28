package com.example.allinone

import android.app.Application
import net.sqlcipher.database.SQLiteDatabase

class AllInOneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        DataManager.loadData(this)
    }
}