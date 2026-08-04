package com.example.allinone.feature.idea.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.allinone.BaseActivity
import com.example.allinone.R
import com.example.allinone.feature.idea.viewmodel.AddIdeaViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * AddIdeaActivity (Modern Architecture): Feature UI layer for creating and editing ideas.
 * Delegates state management to AddIdeaViewModel, validation to IdeaValidator,
 * and data operations to IdeaRepository.
 */
@AndroidEntryPoint
class AddIdeaActivity : BaseActivity() {

    private val viewModel: AddIdeaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_idea)
        
        val ideaId = intent.getLongExtra("IDEA_ID", -1L)
        viewModel.initialize(ideaId)
    }

    fun onSaveClicked() {
        viewModel.saveIdea(
            onSuccess = {
                Toast.makeText(this, "Idea saved", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun onDeleteClicked() {
        viewModel.deleteIdea {
            Toast.makeText(this, "Idea deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
