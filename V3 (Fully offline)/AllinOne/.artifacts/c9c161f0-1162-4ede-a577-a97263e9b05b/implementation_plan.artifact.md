# Implementation Plan - Restrict Header Edit Section to Dashboard

The user wants to conditionally show the "edit" section (the Edit Project button) in the workspace header only when the "Dashboard" tab is active. Currently, it is shown on all other tabs EXCEPT the Dashboard.

## Proposed Changes

### [Component Name]

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)

Update `WorkspaceHeader` to:
1. Move the `IconButton` for `onEditProject` into the `if (currentTab == WorkspaceTab.Dashboard)` block.
2. Remove the `else` block that previously showed the Edit button for other tabs.

```kotlin
        Box {
            if (currentTab == WorkspaceTab.Dashboard) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Added Edit button here
                    IconButton(onClick = onEditProject, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Project", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onImportRequest) { Icon(Icons.Default.UploadFile, contentDescription = "Import", tint = Color.White.copy(alpha = 0.7f)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showProjectMenu = true }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp)); Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // The else block is removed
            DropdownMenu(expanded = showProjectMenu, onDismissRequest = { showProjectMenu = false }) {
                // ...
            }
        }
```

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Workspace.
- Select the **Dashboard** tab and verify that the "Edit" button is visible alongside "Import" and "Switch".
- Navigate to other tabs (e.g., "TASKS", "IDEAS") and verify that the "Edit" button is NO LONGER visible in the header.
