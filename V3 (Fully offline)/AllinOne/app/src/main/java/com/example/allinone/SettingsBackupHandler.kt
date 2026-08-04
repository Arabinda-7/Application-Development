package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.allinone.core.utils.UIUtils
import kotlinx.coroutines.launch

class SettingsBackupHandler(
    private val context: Context,
    private val exportLauncher: ActivityResultLauncher<String>,
    private val importLauncher: ActivityResultLauncher<Array<String>>,
    private val scope: LifecycleCoroutineScope
) {
    fun exportBackup() {
        UIUtils.showPasswordDialog(context, "ENCRYPT BACKUP") { password ->
            currentPassword = password
            exportLauncher.launch("allinone_backup_${System.currentTimeMillis()}.json")
        }
    }

    private var currentPassword: CharArray? = null

    fun getExportedJson(onResult: (String) -> Unit) {
        scope.launch {
            val json = DataManager.exportData(context, currentPassword)
            onResult(json)
            currentPassword = null // Clear after use
        }
    }

    fun importBackup() {
        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
    }

    fun handleImport(content: String, onImportSuccess: () -> Unit) {
        val isEncrypted = !content.trim().startsWith("{")
        
        if (isEncrypted) {
            UIUtils.showPasswordDialog(context, "DECRYPT BACKUP") { password ->
                performImport(content, password, onImportSuccess)
            }
        } else {
            showConfirmationDialog("RESTORE DATA", "Overwrite all current app data?", "RESTORE NOW") {
                performImport(content, null, onImportSuccess)
            }
        }
    }

    private fun performImport(content: String, password: CharArray?, onImportSuccess: () -> Unit) {
        scope.launch {
            if (DataManager.importData(context, content, password)) {
                showRestartDialog()
            } else {
                val msg = if (password != null) "Incorrect password or corrupted file" else "Import Failed: Incompatible file"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRestartDialog() {
        showConfirmationDialog(
            "IMPORT SUCCESSFUL",
            "Data has been restored. The app needs to restart to apply all changes correctly. Restart now?",
            "RESTART APP"
        ) {
            restartApp()
        }
    }

    private fun restartApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        // Kill process to ensure fresh Hilt/Room state
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }

    private fun showConfirmationDialog(title: String, message: String, pos: String, onConfirm: () -> Unit) {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_confirm_backup)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        dialog.findViewById<TextView>(R.id.btn_confirm_positive).apply { text = pos; setOnClickListener { onConfirm(); dialog.dismiss() } }
        dialog.findViewById<android.view.View>(R.id.btn_confirm_negative).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
