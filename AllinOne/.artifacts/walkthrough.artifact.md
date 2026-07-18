# Walkthrough - Fix Unresolved reference 'etInspirationUrl'

I have resolved the build error `Unresolved reference 'etInspirationUrl'` in `ProjectActivity.kt`. The issue was caused by a missing variable declaration for the inspiration URL input field in the Project Idea dialog.

## Changes Made

### ProjectActivity.kt

I updated the `showAddIdeaDialog` method to:
- Properly initialize `etInspirationUrl` by finding it in the dialog layout.
- Ensure that the `inspirationUrl` is saved to the `Note` object when the "Save" or "Update" button is clicked.

```diff
<<<<
        val btnPriority = dialog.findViewById<TextView>(R.id.btn_priority_tag)
        val tvCharCount = dialog.findViewById<TextView>(R.id.tv_char_count)

        val containerSubfeatures = dialog.findViewById<LinearLayout>(R.id.container_subfeatures)
====
        val btnPriority = dialog.findViewById<TextView>(R.id.btn_priority_tag)
        val tvCharCount = dialog.findViewById<TextView>(R.id.tv_char_count)
        val etInspirationUrl = dialog.findViewById<EditText>(R.id.et_inspiration_url)

        val containerSubfeatures = dialog.findViewById<LinearLayout>(R.id.container_subfeatures)
>>>>
<<<<
                idea.title = title
                idea.content = contentInput.text.toString()
                idea.priority = currentPriority
                idea.vibeColor = selectedVibeColor
                idea.subFeatures.clear()
====
                idea.title = title
                idea.content = contentInput.text.toString()
                idea.priority = currentPriority
                idea.vibeColor = selectedVibeColor
                idea.inspirationUrl = etInspirationUrl.text.toString()
                idea.subFeatures.clear()
>>>>
```

## Verification Results

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin` and it finished successfully.

### Manual Verification
- You can now add and edit inspiration URLs in the Project Idea dialog, and they will be correctly persisted.
