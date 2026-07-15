package com.example.allinone

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private var appliedDisplaySize: String = ""
    private var appliedFontSize: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        appliedDisplaySize = DataManager.displaySize
        appliedFontSize = DataManager.fontSize
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(UIUtils.wrapContext(newBase))
    }

    override fun onResume() {
        super.onResume()
        // Check if global scales changed while this activity was in backstack
        if (appliedDisplaySize != DataManager.displaySize || appliedFontSize != DataManager.fontSize) {
            recreate()
        }
    }
}