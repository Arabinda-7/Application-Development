package com.example.allinone.workspace.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.allinone.AppStyle
import com.example.allinone.BaseActivity
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.WorkspaceDatabase
import com.example.allinone.workspace.domain.WorkspaceRepository
import com.example.allinone.workspace.ui.ProjectWorkspaceScreen
import com.example.allinone.workspace.ui.WorkspaceViewModel

class WorkspaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val database = WorkspaceDatabase.getDatabase(this)
            val repository = WorkspaceRepository(database.workspaceDao())
            
            val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorkspaceViewModel(repository) as T
                }
            })[WorkspaceViewModel::class.java]

            setContent {
                val style = AppStyle() 

                CompositionLocalProvider(LocalAppStyle provides style) {
                    ProjectWorkspaceScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WorkspaceActivity", "Crash in WorkspaceActivity.onCreate", e)
            android.widget.Toast.makeText(this, "Error opening Workspace: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
