package com.example.allinone.feature.project.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.allinone.BaseActivity
import com.example.allinone.R
import com.example.allinone.feature.project.viewmodel.EditProjectViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * EditProjectActivity (Modern Architecture): UI controller delegating state & logic to EditProjectViewModel.
 */
@AndroidEntryPoint
class EditProjectActivity : BaseActivity() {

    private val viewModel: EditProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        val projectId = intent.getLongExtra("PROJECT_ID", -1L)
        viewModel.initialize(projectId)
    }

    fun onSaveClicked() {
        viewModel.saveProject(
            onSuccess = {
                Toast.makeText(this, "Project saved", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun onDeleteClicked() {
        viewModel.deleteProject {
            Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
