package com.example.allinone.workspace.ui.sections

import androidx.compose.runtime.Composable
import com.example.allinone.workspace.data.FeatureEntity
import com.example.allinone.workspace.data.WorkspaceTaskEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel
import com.example.allinone.workspace.ui.sections.features.FeatureAddEditSection as NewFeatureAddEditSection
import com.example.allinone.workspace.ui.sections.features.FeatureDetailSection as NewFeatureDetailSection
import com.example.allinone.workspace.ui.sections.features.FeatureViewSection as NewFeatureViewSection

@Composable
fun FeatureViewSection(
    features: List<FeatureEntity>,
    tasks: List<WorkspaceTaskEntity>,
    viewModel: WorkspaceViewModel,
    onViewFeature: (FeatureEntity) -> Unit,
    onEditFeature: (FeatureEntity) -> Unit,
    onDeleteFeature: (FeatureEntity) -> Unit
) {
    NewFeatureViewSection(features, tasks, viewModel, onViewFeature, onEditFeature, onDeleteFeature)
}

@Composable
fun FeatureDetailSection(
    feature: FeatureEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    NewFeatureDetailSection(feature, onBack, onEdit)
}

@Composable
fun FeatureAddEditSection(
    feature: FeatureEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    NewFeatureAddEditSection(feature, projectId, viewModel, onBack)
}
