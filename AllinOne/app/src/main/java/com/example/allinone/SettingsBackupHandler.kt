package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch

class SettingsBackupHandler(
    private val context: Context,
    private val exportLauncher: ActivityResultLauncher<String>,
    private val importLauncher: ActivityResultLauncher<Array<String>>,
    private val scope: LifecycleCoroutineScope
) {
    fun exportBackup() {
        exportLauncher.launch("allinone_backup_${System.currentTimeMillis()}.json")
    }

    fun importBackup() {
        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
    }

    fun handleImport(content: String, onImportSuccess: () -> Unit) {
        showConfirmationDialog("RESTORE DATA", "Overwrite all current app data?", "RESTORE NOW") {
            scope.launch {
                if (DataManager.importData(context, content)) {
                    android.widget.Toast.makeText(context, "Data Restored Successfully", android.widget.Toast.LENGTH_LONG).show()
                    onImportSuccess()
                } else {
                    android.widget.Toast.makeText(context, "Import Failed: Incompatible file", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showConfirmationDialog(title: String, message: String, pos: String, onConfirm: () -> Unit) {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        dialog.findViewById<TextView>(R.id.btn_confirm_positive).apply { text = pos; setOnClickListener { onConfirm(); dialog.dismiss() } }
        dialog.findViewById<android.view.View>(R.id.btn_confirm_negative).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
