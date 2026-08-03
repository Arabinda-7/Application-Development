package com.example.allinone.feature.project.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.allinone.BaseActivity
import com.example.allinone.R
import com.example.allinone.feature.project.viewmodel.AddProjectViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * AddProjectActivity (Modern Architecture): UI controller bound to AddProjectViewModel.
 */
@AndroidEntryPoint
class AddProjectActivity : BaseActivity() {

    private val viewModel: AddProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
    }

    fun onSaveClicked() {
        viewModel.saveProject(
            onSuccess = {
                Toast.makeText(this, "Project created", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
