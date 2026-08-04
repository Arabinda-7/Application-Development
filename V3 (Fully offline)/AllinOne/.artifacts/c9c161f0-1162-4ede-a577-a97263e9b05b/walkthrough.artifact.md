# Walkthrough - Restrict and Refine Header Edit Section

I have updated the `WorkspaceHeader` to restrict the "Edit Project" button to the Dashboard tab and refined its appearance to be more subtle.

## Changes

### 1. Header Visibility Logic
- The "Edit Project" button is now only visible when the **Dashboard** tab is selected.
- It has been integrated into the dashboard's secondary controls row (alongside Import and Switch).

### 2. Edit Icon Refinement
- Reduced the size of the Edit `IconButton` to **32dp** (from the default 48dp).
- Reduced the size of the Edit `Icon` itself to **16dp**.
- This makes the header feel more balanced and ensures the project title remains the primary focus.

```kotlin
IconButton(
    onClick = onEditProject,
    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
) {
    Icon(
        Icons.Default.Edit,
        modifier = Modifier.size(16.dp),
        ...
    )
}
```

## Verification Results

### Manual Verification
- **Dashboard Tab**: Verified that the Edit icon is now smaller and neatly aligned with the other controls.
- **Other Tabs**: Verified the Edit icon remains hidden.
