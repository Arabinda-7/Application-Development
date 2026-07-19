package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private var appliedDisplaySize: String = ""
    private var appliedFontSize: String = ""
    private var activeDialog: Dialog? = null

    fun showDialogSafe(dialog: Dialog) {
        if (activeDialog?.isShowing == true) return
        activeDialog = dialog
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppTheme()
        appliedDisplaySize = DataManager.displaySize
        appliedFontSize = DataManager.fontSize
        super.onCreate(savedInstanceState)
    }

    private fun applyAppTheme() {
        if (DataManager.isSystemAppearanceEnabled) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            val mode = when(DataManager.appThemeMode) {
                "LIGHT" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                "DARK", "OLED" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
            
            if (DataManager.appThemeMode == "OLED") {
                window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
            }
        }
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