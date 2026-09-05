# 💰 Expense Tracker — Ultimate Personal & Shared Finance

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange.svg)](https://dagger.dev/hilt/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A high-performance, privacy-focused financial management ecosystem for Android. This isn't just an expense logger—it's a complete financial suite featuring **Clean Architecture**, **MVI/MVVM design patterns**, and a custom **Node.js notification backbone** to keep your shared finances in sync without the cost of premium cloud services.

---

## 🌟 Comprehensive Feature Set

### 1. 📔 Personal Ledger & Core Tracking
*   **Omni-Channel Entry**: Rapidly log income and expenses with detailed metadata (notes, dates, categories).
*   **Advanced Filtering**: Slice through your data by category, date range, or transaction type.
*   **Soft Deletion & Restore**: Robust data safety with undo/restore capabilities for accidentally deleted entries.
*   **Custom Category Engine**: Organize your life with custom icons and color-coded categories.

### 2. 👥 "Split With Friends" (Social Finance)
*   **Group Dynamics**: Create dedicated groups for shared apartments, travel, or events.
*   **Complex Split Logic**:
    *   **Equally**: Simple split among members.
    *   **Exact Amounts**: Specify who owes exactly what.
    *   **Percentages**: Split by custom percentage weights.
*   **Debt Simplification Algorithm**: An advanced graph-based logic that minimizes the total number of payments needed to settle up within a group.
*   **Interactive Comments**: Discuss specific expenses and attach context directly within the transaction.
*   **Real-time Activity Log**: A chronological feed of who added, edited, or settled expenses.

### 3. 🎯 Budgeting & Goal Architecture
*   **Granular Budgeting**: Set hard limits on specific categories with dynamic progress tracking.
*   **Savings Goals**: Define long-term targets (e.g., "New Car" or "Emergency Fund") with dedicated contribution tracking.
*   **Threshold Alerts**: Visual cues when you're approaching or exceeding your monthly budget.

### 4. 🤖 Intelligence & Analytics
*   **Visual Spending Trends**: High-fidelity line and bar charts powered by **Vico**.
*   **Category Breakdowns**: Interactive pie/donut charts for instant spending awareness.
*   **Anomaly Detection**: AI-driven detection of unusual spending spikes or patterns.
*   **Month-over-Month Growth**: Comparative analysis to track your financial health over time.
*   **Heatmaps**: Temporal visualization of when you spend the most.

### 5. ⚙️ Automation & Utility
*   **Recurring Engine**: Set-and-forget logic for monthly rent, subscriptions, or salaries.
*   **Smart Reminders**: System-level notifications for upcoming bill due dates.
*   **Biometric Security**: Industry-standard Fingerprint and Face Unlock integration via BiometricPrompt.
*   **Jetpack Glance Widgets**: Zero-click balance tracking and "Quick Add" actions directly from your home screen.

---

## 🛠 Technical Excellence

### Architecture
The project is built on **Clean Architecture**, enforcing a strict unidirectional data flow (UDF):
- **Domain Layer**: Pure Kotlin. Contains `UseCases` (the verbs of the app), `Repository` interfaces, and pure domain entities.
- **Data Layer**: Implements the repositories. Manages **Room** (local cache), **Firestore** (remote sync), and the **Sync Outbox** pattern for offline-first reliability.
- **Presentation Layer**: 100% **Jetpack Compose**. Uses `ViewModels` to manage UI state and process user `Intents` (MVI).

### The "Firebase Spark" Notification Server
Included in the repo is a custom Node.js service (`notification-server/`). 
- **The Problem**: Firebase Cloud Functions require a paid "Blaze" plan for Firestore triggers.
- **The Solution**: This lightweight server uses the Admin SDK to listen to Firestore via `onSnapshot` (free tier) and pushes notifications via FCM, providing a production-grade experience on a $0 budget.

### Tech Stack Summary
- **Language**: Kotlin + Coroutines + Flow
- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **DB**: Room (Persistence) + Firestore (Sync)
- **Networking**: OkHttp + Retrofit (for server comms)
- **Charts**: Vico Charts
- **Widgets**: Jetpack Glance
- **Testing**: JUnit 5, Mockk, Hilt Testing, Espresso

---

## 🚀 Getting Started

### 📦 Setup Instructions
1.  **Clone & Sync**:
    ```bash
    git clone https://github.com/anujomer/expensetracker.git
    ```
2.  **Firebase Configuration**:
    - Create a project on the [Firebase Console](https://console.firebase.google.com/).
    - Enable **Firestore** and **Authentication** (Email/Google).
    - Drop `google-services.json` into the `app/` folder.
3.  **Local Environment**:
    - Ensure you are using **Android Studio Ladybug (2024.2.1)** or newer.
    - Gradle will automatically handle the dependency resolution.

### 🔔 Setting up Notifications
See the [Notification Server README](notification-server/README.md) for detailed instructions on deploying the Node.js bridge to Render or Fly.io.

---

## 🛡 License & Contribution

- **License**: Licensed under the [MIT License](LICENSE).
- **Contributions**: Contributions are welcome! Please open an issue or submit a PR for any features or bug fixes.

---
**Developed with ❤️ by [Anuj Omer](https://github.com/ANUJOMER21)**
