package com.example.allinone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView

class PerformanceHistoryActivity : BaseActivity() {

    private val viewModel: PerformanceHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val composeView = ComposeView(this)
        setContentView(composeView)

        PerformanceHistoryComposeHandler(composeView, viewModel) {
            finish()
        }.setup()
    }
}
