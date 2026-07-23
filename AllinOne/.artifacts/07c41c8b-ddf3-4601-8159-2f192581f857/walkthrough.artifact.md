# Walkthrough: Detailed Agenda with Navigation Paths

I have transformed the "Today's Agenda" into a rich, interactive notification center. Agenda items now display detailed metadata and breadcrumb paths, and they support direct navigation to their respective sections in the app.

## Changes Made

### Rich Data Model
- **`AgendaItem`**: Introduced a new data class to hold title, details, paths, and navigation logic.
- **Deep Scanning with Paths**: Updated `DataManager` to not only find deadlines but also construct their "location" within the app (e.g., `Workspace > AllInOne > Goals`).

### UI & UX Enhancements
- **Interactive Agenda**: The notification dialog now uses a modern card layout for each item.
- **Detailed Metadata**: Items show secondary info like Priority (High/Medium/Low), Weights, Progress percentages, or Health status.
- **Breadcrumb Navigation**: Each item displays its path with a location icon, making it clear where the task or milestone belongs.
- **Direct Navigation**: Clicking any item in the agenda will now automatically navigate you to the correct activity or workspace section.

## Verification Results

### Automated Validation
- **Path Accuracy**: Verified that Workspace items correctly resolve their parent project names for the breadcrumb path.
- **Priority Mapping**: Confirmed that numeric priority values (0-2) are correctly translated into human-readable strings.

### Manual Verification
- Verified that clicking a legacy task navigates to `TaskActivity`.
- Verified that clicking a Roadmap item navigates to `ProjectActivity`.
- Verified that clicking a Workspace item navigates to `WorkspaceActivity`.
- Confirmed that the "red dot" and dialog list are perfectly synchronized with the new detailed data.
