# 🌟 All in One: Your Personal Productivity & Life Headquarters

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-31%20(Android%2012)-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20Arch-FF6F00.svg)](https://developer.android.com/topic/architecture)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline--First-000000.svg)](#-privacy-security--customization)

**All in One** is a holistic, offline-first life-management system for Android. Built with modern Jetpack Compose and local-first encryption, it centralizes daily planning, habits, financial tracking, mind mapping, health routines, and AI assistance into a single customizable dashboard.

---

## 📸 Screenshots & UI Showcase

| Home Dashboard | Habit Heatmap | Idea Mind Map |
| :---: | :---: | :---: |
| *(Add Image Link)* | *(Add Image Link)* | *(Add Image Link)* |

| Financial Ledger | Workout Tracker | AI Assistant |
| :---: | :---: | :---: |
| *(Add Image Link)* | *(Add Image Link)* | *(Add Image Link)* |

---

## 🚀 Key Modules & Features

### 🤖 Intelligent AI Assistant & Voice Engine
- **Assistant Brain:** Adaptive AI session logging, follow-up prompt engines, and contextual suggestions.
- **Voice Interaction Manager:** Hands-free voice commands to add tasks, notes, or query daily progress.
- **Session History:** Track past assistant interactions and recommendations over time.

### 📅 Daily Planning & Task Engineering
- **Categorical To-Dos:** Manage complex task lists with subtasks, priority levels, and custom tags.
- **Habit Tracker & Visual Heatmaps:** Monitor consistency with daily grid heatmaps, streak tracking, and growth insights.
- **Universal Search:** Instant search across all tasks, notes, habits, ledger entries, and project milestones.

### 💡 Project & Knowledge Management
- **Project Workspaces:** Track multi-phase projects with sub-features, progress bars, and history logs.
- **Interactive Mind Maps:** Visualize creative concepts and expand branch hierarchies dynamically.
- **Architect Tree:** Structured hierarchical views for long-term project planning.

### 💰 Financial Wellness & Ledger Hub
- **Personal Ledger Book:** Double-entry ledger tracking for transactions, debts, and person-to-person balances.
- **Budgeting & Safe-to-Spend:** Real-time calculation of remaining monthly budget and safe spending thresholds.
- **Finance Analytics:** Category breakdown charts and month-over-month expenditure heatmaps.

### 🏋️ Health & Fitness Tracking
- **Workout Routines:** Custom routine builders, set/rep logging, and specialized exercise adapters.
- **Performance Dashboard:** Unified score summarizing overall progress across productivity, health, habits, and finance.

### 🔐 Privacy, Security & Customization
- **100% Offline-First:** Zero mandatory cloud sync; all personal data stays strictly on your device.
- **Biometric Security:** App lock backed by Android Biometrics (Fingerprint / Face Unlock) and encrypted storage.
- **Glassmorphism Design Engine:** Adjustable blur effects, dark/OLED modes, dynamic accent color palettes, and custom typography (Serif, Monospace, Sans-Serif).

---

## 🛠 Tech Stack & Architecture

### Architecture
Built following **Clean Architecture + MVVM (Model-View-ViewModel)** guidelines with unidirectional data flow (UDF).

```
UI (Compose / XML) ──> ViewModel ──> Domain UseCases ──> Repository ──> Local Room DB / Encrypted Store
```

### Core Libraries & Technologies
- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose (Material 3 Design System) + Hybrid XML Views
- **Dependency Injection:** Dagger Hilt
- **Local Persistence:** Room Database with KSP (Kotlin Symbol Processing)
- **Concurrency & State:** Kotlin Coroutines, StateFlow, and SharedFlow
- **Security:** AndroidX Biometric, AndroidX Security Crypto, SQLCipher (Encrypted SQLite)
- **Serialization:** Kotlinx Serialization & Google Gson

---

## 📂 Project Structure

```text
com.example.allinone/
├── assistant/         # AI Assistant Brain, Voice Manager, & Follow-up Engines
├── backup/            # Data backup & restore handlers
├── core/              # Core utilities, theme tokens, and base components
├── data/              # Room DAOs, Mappers, Repositories, & DataManager
├── di/                # Dagger Hilt Dependency Injection modules
├── domain/            # Domain models and business logic Use Cases
├── feature/           # Modularized feature implementations
├── security/          # Biometric authentication & security lock handlers
├── ui/                # Compose screens, theme managers, & adapters
└── workspace/         # Project workspace management screens
```

---

## 🏃 Getting Started

### Prerequisites
- **Android Studio:** Koala (2024.1.1) or newer
- **JDK:** Java 17
- **Device / Emulator:** Android 12.0 (API Level 31) or higher

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/AllinOne.git
   cd AllinOne
   ```
2. **Open in Android Studio:** Select `Open an Existing Project` and navigate to the project directory.
3. **Build Project:** Wait for Gradle sync to complete and press **Run** (`Shift + F10`).

---

## 🗺️ Roadmap

- [x] AI Assistant Engine & Voice Manager
- [x] Personal Ledger & Budgeting
- [x] Mind Mapping & Architect Tree
- [ ] Local File Encrypted Backup & Restore (ZIP / JSON)
- [ ] PDF & CSV Export for Financial Reports
- [ ] Interactive Desktop / Tablet Optimized Layouts

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---

Created with ❤️ by **Arabinda**
