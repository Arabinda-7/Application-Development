package com.example.allinone

import android.app.Dialog
import com.example.allinone.security.SecurityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.allinone.core.utils.UIUtils

open class BaseActivity : AppCompatActivity() {
    private var appliedDisplaySize: String = ""
    private var appliedFontSize: String = ""
    private var activeDialog: Dialog? = null

    private var onPermissionGranted: (() -> Unit)? = null
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            onPermissionGranted?.invoke()
        }
    }

    private var onPermissionsResult: ((Map<String, Boolean>) -> Unit)? = null
    private val multiplePermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        onPermissionsResult?.invoke(result)
    }

    fun checkAndRequestPermission(permission: String, onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            else -> {
                onPermissionGranted = onGranted
                permissionLauncher.launch(permission)
            }
        }
    }

    fun checkAndRequestPermissions(permissions: Array<String>, onResult: (Map<String, Boolean>) -> Unit) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isEmpty()) {
            val allGranted = permissions.associateWith { true }
            onResult(allGranted)
        } else {
            onPermissionsResult = onResult
            multiplePermissionsLauncher.launch(permissionsToRequest)
        }
    }

    fun showDialogSafe(dialog: Dialog) {
        if (activeDialog?.isShowing == true) return
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 20
        }
        activeDialog = dialog
        dialog.show()
    }

    fun showStyledConfirmationDialog(
        title: String,
        message: String,
        actionText: String = "CONFIRM",
        actionColor: Int = Color.parseColor("#1A73E8"),
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_confirm)
        
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_confirm_cancel)
        val btnAction = dialog.findViewById<TextView>(R.id.btn_confirm_action)

        btnAction.text = actionText
        btnAction.backgroundTintList = ColorStateList.valueOf(actionColor)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAction.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        showDialogSafe(dialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        applyAppTheme()
        SecurityManager.setScreenshotProtection(this, DataManager.isScreenshotProtectionEnabled)
        appliedDisplaySize = DataManager.displaySize
        appliedFontSize = DataManager.fontSize
        super.onCreate(savedInstanceState)
    }

    protected fun setupKeyboardHandling(rootView: View, contentView: View? = null, extraTopPaddingDp: Int = 0) {
        val extraTopPx = (extraTopPaddingDp * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply bottom padding to the root view (handles keyboard + navigation bar)
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, imeInsets.bottom.coerceAtLeast(systemBars.bottom))
            
            // Apply top padding to the content view only (handles status bar)
            contentView?.setPadding(contentView.paddingLeft, systemBars.top + extraTopPx, contentView.paddingRight, contentView.paddingBottom)
            
            insets
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun applyAppTheme() {
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